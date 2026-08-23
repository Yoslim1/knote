from pathlib import Path
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res"

def keys(path):
    tree = ET.parse(path)
    out = {"string": set(), "plurals": set(), "string-array": set()}
    for node in tree.getroot():
        name = node.attrib.get("name")
        if node.tag in out and name:
            out[node.tag].add(name)
    return out

def merge(paths):
    result = {"string": set(), "plurals": set(), "string-array": set()}
    for path in paths:
        current = keys(path)
        for kind in result:
            result[kind] |= current[kind]
    return result

def main():
    default = merge(sorted((root / "values").glob("*.xml")))
    arabic = merge(sorted((root / "values-ar").glob("*.xml")))
    print("resource kind | default keys | ar keys | missing in ar")
    print("---|---:|---:|---:")
    has_missing = False
    for kind in ("string", "plurals", "string-array"):
        missing = sorted(default[kind] - arabic[kind])
        has_missing = has_missing or bool(missing)
        print(f"{kind} | {len(default[kind])} | {len(arabic[kind])} | {len(missing)}")
        if missing:
            print("  " + ", ".join(missing[:30]) + (" ..." if len(missing) > 30 else ""))
    if has_missing:
        raise SystemExit("localized resource parity check failed")

if __name__ == "__main__":
    main()
