from __future__ import annotations

import json
import os
import re
import xml.etree.ElementTree as ET
from pathlib import Path

from openai import OpenAI

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app" / "src" / "main" / "res"
DEFAULT_DIR = RES / "values"
AR_DIR = RES / "values-ar"
OUTPUT = AR_DIR / "strings_ui.xml"
BATCH_SIZE = 24


def element_payload(node: ET.Element) -> dict:
    if node.tag == "string":
        return {"key": node.attrib["name"], "kind": "string", "values": ["".join(node.itertext())]}
    if node.tag in {"plurals", "string-array"}:
        return {
            "key": node.attrib["name"],
            "kind": node.tag,
            "values": ["".join(item.itertext()) for item in node.findall("item")],
        }
    raise ValueError(node.tag)


def load_nodes(directory: Path) -> dict[str, dict]:
    result: dict[str, dict] = {}
    for path in sorted(directory.glob("*.xml")):
        root = ET.parse(path).getroot()
        for node in root:
            if node.tag in {"string", "plurals", "string-array"} and "name" in node.attrib:
                result[node.attrib["name"]] = element_payload(node)
    return result


def xml_escape(value: str) -> str:
    return (
        value.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace('"', "&quot;")
        .replace("'", "\\'")
    )


def validate_placeholder(source: str, translated: str) -> None:
    token = re.compile(r"%(?:\\d+\\$)?[sdif]|%")
    source_tokens = sorted(token.findall(source))
    translated_tokens = sorted(token.findall(translated))
    if source_tokens != translated_tokens:
        raise ValueError(f"placeholder mismatch: {source!r} -> {translated!r}")


def translate_batch(client: OpenAI, batch: list[dict]) -> list[dict]:
    prompt = {
        "task": "Translate Android UI resources from English to natural modern Arabic.",
        "rules": [
            "Return JSON only with an items array.",
            "Keep the same order, key, kind, and number of values.",
            "Preserve every Android format placeholder exactly, such as %1$s, %2$d, %d, and %%.",
            "Do not translate resource keys, technical identifiers, markdown syntax, hashtags, or product name Knote.",
            "Use concise UI Arabic appropriate for buttons, settings, dialogs, notifications, and accessibility labels.",
            "For plurals, translate each provided quantity variant in the same order; preserve %d.",
            "For arrays, translate each item in the same order.",
            "Do not add explanations, quotes outside JSON, or markdown.",
        ],
        "items": batch,
    }
    response = client.chat.completions.create(
        model="gpt-5-mini",
        messages=[
            {"role": "system", "content": "You are a meticulous Arabic Android localization editor."},
            {"role": "user", "content": json.dumps(prompt, ensure_ascii=False)},
        ],
        response_format={
            "type": "json_schema",
            "json_schema": {
                "name": "arabic_android_translations",
                "strict": True,
                "schema": {
                    "type": "object",
                    "properties": {
                        "items": {
                            "type": "array",
                            "items": {
                                "type": "object",
                                "properties": {
                                    "key": {"type": "string"},
                                    "kind": {"type": "string"},
                                    "values": {"type": "array", "items": {"type": "string"}},
                                },
                                "required": ["key", "kind", "values"],
                                "additionalProperties": False,
                            },
                        }
                    },
                    "required": ["items"],
                    "additionalProperties": False,
                },
            },
        },
        max_completion_tokens=12000,
    )
    return json.loads(response.choices[0].message.content)["items"]


def main() -> None:
    default = load_nodes(DEFAULT_DIR)
    arabic = load_nodes(AR_DIR)
    missing = [default[key] for key in sorted(default) if key not in arabic]
    client = OpenAI()
    translated: dict[str, dict] = {}
    for start in range(0, len(missing), BATCH_SIZE):
        batch = missing[start : start + BATCH_SIZE]
        result = translate_batch(client, batch)
        if len(result) != len(batch):
            raise ValueError(f"batch size mismatch at {start}: {len(result)} != {len(batch)}")
        for expected, actual in zip(batch, result):
            if expected["key"] != actual["key"] or expected["kind"] != actual["kind"]:
                raise ValueError(f"key mismatch: {expected} != {actual}")
            if len(expected["values"]) != len(actual["values"]):
                retry = translate_batch(client, [expected])
                if len(retry) != 1 or len(retry[0]["values"]) != len(expected["values"]):
                    raise ValueError(f"value count mismatch for {expected['key']}")
                actual = retry[0]
            for source, target in zip(expected["values"], actual["values"]):
                validate_placeholder(source, target)
            translated[actual["key"]] = actual
        print(f"translated {min(start + BATCH_SIZE, len(missing))}/{len(missing)}", flush=True)

    lines = [
        '<resources xmlns:tools="http://schemas.android.com/tools">',
        "    <!-- Arabic UI translations generated from the English resource contract. -->",
    ]
    for key in sorted(translated):
        item = translated[key]
        if item["kind"] == "string":
            lines.append(f'    <string name="{key}">{xml_escape(item["values"][0])}</string>')
        elif item["kind"] == "plurals":
            lines.append(f'    <plurals name="{key}">')
            source = default[key]
            for source_value, value in zip(source["values"], item["values"]):
                quantity = "other"
                source_node = None
                for path in sorted(DEFAULT_DIR.glob("*.xml")):
                    root = ET.parse(path).getroot()
                    for node in root.findall("plurals"):
                        if node.attrib.get("name") == key:
                            source_node = node
                            break
                    if source_node is not None:
                        break
                if source_node is not None:
                    index = source["values"].index(source_value)
                    quantity = source_node.findall("item")[index].attrib["quantity"]
                lines.append(f'        <item quantity="{quantity}">{xml_escape(value)}</item>')
            lines.append("    </plurals>")
        elif item["kind"] == "string-array":
            lines.append(f'    <string-array name="{key}">')
            for value in item["values"]:
                lines.append(f"        <item>{xml_escape(value)}</item>")
            lines.append("    </string-array>")
    lines.append("</resources>")
    OUTPUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"wrote {OUTPUT} with {len(translated)} resources")


if __name__ == "__main__":
    main()
