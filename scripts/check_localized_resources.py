#!/usr/bin/env python3
"""Check that every Compose string resource locale stays in sync with English."""

from pathlib import Path
import sys
import xml.etree.ElementTree as ET


RESOURCE_ROOT = Path(__file__).resolve().parents[1] / "shared/src/commonMain/composeResources"
BASE_FILE = RESOURCE_ROOT / "values/strings.xml"


def names(path: Path) -> set[str]:
    root = ET.parse(path).getroot()
    result: set[str] = set()
    for element in root:
        if element.tag not in {"string", "plurals", "string-array"}:
            continue
        name = element.attrib.get("name")
        if not name:
            raise ValueError(f"Resource without a name in {path}")
        if name in result:
            raise ValueError(f"Duplicate resource '{name}' in {path}")
        result.add(name)
    return result


def main() -> int:
    base = names(BASE_FILE)
    failed = False
    for path in sorted(RESOURCE_ROOT.glob("values*/strings.xml")):
        if path == BASE_FILE:
            continue
        localized = names(path)
        missing, extra = sorted(base - localized), sorted(localized - base)
        if missing or extra:
            failed = True
            print(path.relative_to(RESOURCE_ROOT))
            if missing:
                print("  missing: " + ", ".join(missing))
            if extra:
                print("  unexpected: " + ", ".join(extra))
    if failed:
        return 1
    print(f"Checked {len(list(RESOURCE_ROOT.glob('values*/strings.xml')))} locale resource files.")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (OSError, ET.ParseError, ValueError) as error:
        print(error, file=sys.stderr)
        sys.exit(1)
