#!/usr/bin/env python3
"""
sort_strings.py - Android strings.xml organizer.
Usage: python3 sort_strings.py app/src/main/res/values/strings.xml
"""

import argparse
import sys
import os
import re
from xml.etree import ElementTree as ET

def sort_strings(input_file, output_file=None):
    if output_file is None:
        output_file = input_file

    try:
        with open(input_file, 'r', encoding='utf-8') as f:
            content = f.read()

        match = re.search(r'(<resources[^>]*>)(.*)(</resources>)', content, re.DOTALL)
        if not match:
            print("Could not find <resources> tag.")
            return

        header, body, footer = match.groups()

        # Extract all <string> tags
        pattern = re.compile(r'(<string\s+name="([^"]+)"[^>]*>.*?</string>)', re.DOTALL)
        strings = pattern.findall(body)

        if not strings:
            print("No strings found to sort.")
            return

        # Sort strings by their name
        strings.sort(key=lambda x: x[1])

        # Reconstruct the body with prefix-based comments
        new_body = "\n"
        current_prefix = None

        for full_tag, name in strings:
            prefix = name.split('_')[0] if '_' in name else "misc"

            if prefix != current_prefix:
                new_body += f"\n    <!-- {prefix.capitalize()} -->\n"
                current_prefix = prefix

            new_body += f"    {full_tag}\n"

        new_body += "    "

        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(f"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
            f.write(f"{header}{new_body}{footer}\n")

        print(f"Sorted strings written to {output_file}")

    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)

def main():
    parser = argparse.ArgumentParser(description="Sort Android strings.xml.")
    parser.add_argument("input", help="Path to strings.xml")
    parser.add_argument("-o", "--output", help="Output path")
    args = parser.parse_args()

    if not os.path.exists(args.input):
        print(f"Error: File {args.input} not found.")
        sys.exit(1)

    sort_strings(args.input, args.output)

if __name__ == "__main__":
    main()
