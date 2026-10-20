#!/usr/bin/env python3
"""Make an API-37.0 metadata-only compatibility copy for upstream AGP 8.10.

Does not change android.jar or the original SDK installation. Only API 37.0
(minor zero) is supported; never truncate a later minor/preview SDK version.
Remove this adapter when migrating the fork to a newer Android build toolchain.
"""
import hashlib
import os
from pathlib import Path
import shutil
import sys

sdk = Path(sys.argv[1] if len(sys.argv) > 1 else
           os.environ.get("ANDROID_HOME", os.environ.get("ANDROID_SDK_ROOT", "")))
source = sdk / "platforms" / "android-37.0"
target = sdk / "platforms" / "android-37"

if target.is_dir():
    print("API 37 legacy target already present; no changes made.")
    sys.exit(0)
if not source.is_dir():
    sys.exit("Install the official platforms;android-37.0 package first.")
properties = (source / "source.properties").read_text()
if "AndroidVersion.ApiLevel=37.0\n" not in properties:
    sys.exit("Refusing to adapt an SDK other than release API 37.0.")
metadata = (source / "package.xml").read_text()
if "<api-level>37.0</api-level>" not in metadata:
    sys.exit("SDK metadata does not describe API 37.0.")

shutil.copytree(source, target)
(target / "source.properties").write_text(
    properties.replace("AndroidVersion.ApiLevel=37.0", "AndroidVersion.ApiLevel=37")
    .replace("AndroidVersion.CodeName=\n", ""))
(target / "package.xml").write_text(
    metadata.replace("platforms;android-37.0", "platforms;android-37")
    .replace("<api-level>37.0</api-level>", "<api-level>37</api-level>")
    .replace("<codename></codename>", "").replace("<codename/>", "")
    .replace("http://schemas.android.com/sdk/android/repo/repository2/04",
             "http://schemas.android.com/sdk/android/repo/repository2/03"))

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

assert digest(source / "android.jar") == digest(target / "android.jar")
print("API 37.0 legacy metadata prepared. Official android.jar SHA-256 unchanged.")