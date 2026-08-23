from pathlib import Path
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
files = sorted((root / "app" / "src" / "main" / "res").glob("**/*.xml"))
for path in files:
    ET.parse(path)
print(f"validated {len(files)} XML files")
