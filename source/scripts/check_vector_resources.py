#!/usr/bin/env python3
"""Check literal shared vector path colors; optionally inspect the built APK."""
from pathlib import Path
import re, sys, xml.etree.ElementTree as ET, zipfile
ROOT = Path(__file__).resolve().parents[1]
NAMESPACE = '{http://schemas.android.com/apk/res/android}'
def check(data, label):
    tree = ET.fromstring(data)
    for node in tree.iter():
        for attr in ('fillColor', 'strokeColor'):
            value = node.get(NAMESPACE + attr)
            if value is not None:
                assert re.fullmatch(r'#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{4}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})', value), (label, attr, value)
    return tree
vectors = list((ROOT / 'sharedUi/src/commonMain/composeResources/drawable').glob('*.xml'))
assert vectors
for file in vectors: check(file.read_bytes(), file.name)
print(f'PASS: {len(vectors)} shared vector XML resources have literal path colors')
if len(sys.argv) > 1:
    with zipfile.ZipFile(sys.argv[1]) as archive:
        prefix = 'assets/composeResources/com.music.bitchord.sharedui.resources/drawable/'
        names = [n for n in archive.namelist() if n.startswith(prefix) and n.endswith('.xml')]
        assert names
        for name in names: check(archive.read(name), name)
        assert archive.read(prefix + 'ic_logo.xml') == (ROOT / 'sharedUi/src/commonMain/composeResources/drawable/ic_logo.xml').read_bytes()
        print(f'PASS: {len(names)} APK shared vectors checked; packaged logo matches corrected source')
