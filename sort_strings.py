#!/usr/bin/env python3
"""
sort_strings.py - Android strings.xml organizer.

Sorts <string>, <string-array>, <plurals>, and <integer-array> resources
in an Android strings.xml file alphabetically by their `name` attribute,
grouping them under comment headers derived from the name's prefix
(e.g. "btn_submit_label" -> "Btn").

Requires Python 3.9+ (uses xml.etree.ElementTree.indent).

Usage:
    python3 sort_strings.py app/src/main/res/values/strings.xml
    python3 sort_strings.py strings.xml -o sorted_strings.xml
    python3 sort_strings.py strings.xml --check
    python3 sort_strings.py strings.xml --no-comments --case-sensitive
    python3 sort_strings.py strings.xml --dry-run
    python3 sort_strings.py strings.xml --no-backup

Known limitation: CDATA sections are not preserved as CDATA on
round-trip (Python's xml.etree.ElementTree unwraps them into plain
text on parse). The text content itself is preserved and the output
is still valid, equivalent XML, but if you rely on literal
"<![CDATA[...]]>" markers for HTML-formatted strings, running this
tool will replace them with escaped text instead. Switch to lxml if
that distinction matters for your build.
"""

import argparse
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

RESOURCE_TAGS = {"string", "string-array", "plurals", "integer-array"}

# Preserve common Android namespace prefixes on round-trip instead of
# letting ElementTree invent "ns0:"-style prefixes for them.
ET.register_namespace("tools", "http://schemas.android.com/tools")
ET.register_namespace("xliff", "urn:oasis:names:tc:xliff:document:1.2")


def load_tree(input_file):
    """Parse the XML file, preserving comments as Comment nodes."""
    parser = ET.XMLParser(target=ET.TreeBuilder(insert_comments=True))
    return ET.parse(input_file, parser=parser)


def prefix_of(name):
    return name.split("_", 1)[0] if "_" in name else "misc"


def sort_key(name, case_sensitive):
    return name if case_sensitive else name.casefold()


def split_leading_comments(children):
    """
    Peel off a run of Comment nodes at the very start of <resources>
    (e.g. a license header) so they're kept verbatim at the top of the
    file instead of being treated as regenerable group headers.
    """
    leading = []
    rest = list(children)
    while rest and rest[0].tag is ET.Comment:
        leading.append(rest.pop(0))
    return leading, rest


def reorganize(root, case_sensitive=False, add_comments=True):
    """
    Rebuild the child list of <resources>: keep any leading header
    comment(s) untouched, then emit resource elements sorted by name
    with fresh group-header comments in between. Stray comments found
    between resources are dropped -- their position loses meaning once
    everything is re-sorted (this includes group headers from a
    previous run of this same script). Elements without a usable name
    are kept, pushed to the end, in their original relative order.

    Returns (new_children, duplicate_names, unnamed_count).
    """
    leading_comments, rest = split_leading_comments(list(root))

    resources = []
    seen = set()
    duplicates = []

    for child in rest:
        if child.tag is ET.Comment:
            continue  # regenerated below; see docstring
        resources.append(child)
        name = child.get("name")
        if name is not None:
            if name in seen:
                duplicates.append(name)
            seen.add(name)

    unnamed_count = sum(1 for el in resources if el.get("name") is None)

    def key(el):
        name = el.get("name")
        has_name = name is not None
        return (not has_name, sort_key(name, case_sensitive) if has_name else "")

    resources.sort(key=key)

    new_children = list(leading_comments)
    current_prefix = object()  # sentinel that never equals a real prefix
    for el in resources:
        name = el.get("name")
        if add_comments and name is not None:
            prefix = prefix_of(name)
            if prefix != current_prefix:
                new_children.append(ET.Comment(f" {prefix.capitalize()} "))
                current_prefix = prefix
        new_children.append(el)

    return new_children, duplicates, unnamed_count


def render(tree, indent):
    root = tree.getroot()
    if indent > 0:
        ET.indent(tree, space=" " * indent)
    xml_body = ET.tostring(root, encoding="unicode")
    if not xml_body.endswith("\n"):
        xml_body += "\n"
    return '<?xml version="1.0" encoding="utf-8"?>\n' + xml_body


def backup(path):
    bak_path = Path(str(path) + ".bak")
    bak_path.write_bytes(Path(path).read_bytes())
    return bak_path


def sort_strings(
    input_file,
    output_file=None,
    case_sensitive=False,
    add_comments=True,
    indent=4,
    dry_run=False,
    check_only=False,
    make_backup=True,
):
    output_file = output_file or input_file

    try:
        tree = load_tree(input_file)
    except ET.ParseError as e:
        print(f"Error: {input_file} is not well-formed XML ({e}).", file=sys.stderr)
        return 1

    root = tree.getroot()
    if root.tag != "resources":
        print(f"Error: root element is <{root.tag}>, expected <resources>.", file=sys.stderr)
        return 1

    original_order = [
        c.get("name") for c in root if c.tag in RESOURCE_TAGS and c.get("name") is not None
    ]

    new_children, duplicates, unnamed_count = reorganize(
        root, case_sensitive=case_sensitive, add_comments=add_comments
    )

    if duplicates:
        uniq = sorted(set(duplicates))
        print(f"Warning: duplicate string name(s) found: {', '.join(uniq)}", file=sys.stderr)
    if unnamed_count:
        print(
            f"Warning: {unnamed_count} resource(s) with no 'name' attribute "
            "were moved to the end.",
            file=sys.stderr,
        )

    if check_only:
        sorted_order = [
            c.get("name") for c in new_children
            if c.tag in RESOURCE_TAGS and c.get("name") is not None
        ]
        if original_order == sorted_order:
            print(f"{input_file} is already sorted.")
            return 0
        print(f"{input_file} is NOT sorted.")
        return 1

    root[:] = new_children

    output_xml = render(tree, indent)

    # Sanity-check: make sure what we're about to write actually parses.
    try:
        ET.fromstring(output_xml)
    except ET.ParseError as e:
        print(
            f"Internal error: generated XML failed validation ({e}). Nothing was written.",
            file=sys.stderr,
        )
        return 1

    if dry_run:
        print(output_xml, end="")
        return 0

    if (
        make_backup
        and Path(output_file).exists()
        and Path(output_file).resolve() == Path(input_file).resolve()
    ):
        bak_path = backup(input_file)
        print(f"Backup written to {bak_path}")

    with open(output_file, "w", encoding="utf-8") as f:
        f.write(output_xml)

    print(f"Sorted strings written to {output_file}")
    return 0


def main():
    parser = argparse.ArgumentParser(description="Sort Android strings.xml resources by name.")
    parser.add_argument("input", help="Path to strings.xml")
    parser.add_argument("-o", "--output", help="Output path (default: overwrite input)")
    parser.add_argument(
        "--case-sensitive",
        action="store_true",
        help="Sort case-sensitively (default: case-insensitive)",
    )
    parser.add_argument(
        "--no-comments",
        action="store_true",
        help="Don't insert '<!-- Prefix -->' group headers",
    )
    parser.add_argument(
        "--indent", type=int, default=4, help="Spaces per indent level (default: 4)"
    )
    parser.add_argument(
        "--dry-run", action="store_true", help="Print the result instead of writing a file"
    )
    parser.add_argument(
        "--check",
        action="store_true",
        help="Exit 1 if the file isn't already sorted; write nothing (useful in CI)",
    )
    parser.add_argument(
        "--no-backup",
        action="store_true",
        help="Don't write a .bak file when overwriting the input in place",
    )
    args = parser.parse_args()

    input_path = Path(args.input)
    if not input_path.exists():
        print(f"Error: File {args.input} not found.", file=sys.stderr)
        sys.exit(1)

    exit_code = sort_strings(
        args.input,
        args.output,
        case_sensitive=args.case_sensitive,
        add_comments=not args.no_comments,
        indent=args.indent,
        dry_run=args.dry_run,
        check_only=args.check,
        make_backup=not args.no_backup,
    )
    sys.exit(exit_code)


if __name__ == "__main__":
    main()