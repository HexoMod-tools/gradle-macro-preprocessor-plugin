#!/usr/bin/python3
import sys
import re

_in = sys.stdin.read()
for match in re.finditer(r"(\S+\.(\w+)) > (\S+) FAILED\n\s*(.*)(?:.*\n)*?.*\(\2\.java:(\d+)", _in):
    file = 'src/test/java/' + match[1].replace('.', '/') + '.java'
    line = match[5]
    title = match[3]
    message = match[4]
    print(f"::error file={file},line={line},title=Failed in {title}::{message}")
