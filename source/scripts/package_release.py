#!/usr/bin/env python3
"""Fail closed on release identity, unexpected tests, missing lint, or secret files."""
import glob, hashlib, json, os, re, shutil, subprocess, zipfile
from pathlib import Path
import xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]; os.chdir(root)
build=Path('app/build.gradle.kts').read_text(); version=re.search(r'versionName = "([^"]+)"',build)[1]; code=int(re.search(r'versionCode = (\d+)',build)[1])
dist=Path('dist'); dist.mkdir(exist_ok=True)
tests=[]; unexpected=[]; total=0; skipped=0
for pattern in ('app/build/test-results/testProdReleaseUnitTest/TEST-*.xml','shared/build/test-results/jvmTest/TEST-*.xml','sharedUi/build/test-results/jvmTest/TEST-*.xml'):
    files=glob.glob(pattern)
    if not files: raise SystemExit('Missing test reports: '+pattern)
    for file in files:
        suite=ET.parse(file).getroot()
        for t in suite.iter('testcase'):
            total+=1
            if t.find('skipped') is not None: skipped+=1
            if t.find('failure') is not None or t.find('error') is not None:
                row={'class':t.get('classname'),'test':t.get('name')}; tests.append(row)
                if not (row['class']=='com.music.bitchord.GeniusTest' and row['test']=='live genius search and scraping test with noisy titles'): unexpected.append(row)
if total<1100 or unexpected: raise SystemExit('Unexpected/incomplete test results: '+json.dumps({'total':total,'unexpected':unexpected}))
if os.getenv('QA_OUTCOME')=='failure' and not tests: raise SystemExit('QA task failed without an explained test failure')
lint=ET.parse('app/build/reports/lint-results-prodRelease.xml').getroot()
errors=[x.attrib for x in lint if x.get('severity') in ('Error','Fatal')]
if errors: raise SystemExit('Lint errors: '+json.dumps(errors))
apk=Path('app/build/outputs/apk/prod/release/app-prod-universal-release.apk')
if not apk.is_file(): raise SystemExit('Production universal APK missing')
sdk=Path(os.environ['ANDROID_HOME']); tools=sdk/'build-tools/35.0.0'
sig=subprocess.check_output([str(tools/'apksigner'),'verify','--verbose','--print-certs',str(apk)],text=True)
expected='b369d2ccad6466a8b4034f231e9de20ba0e8d87ea47cde2221fca80ddef11d3b'
assert expected in sig.lower() and 'android debug' not in sig.lower(), 'Not the owner production certificate'
badging=subprocess.check_output([str(tools/'aapt'),'dump','badging',str(apk)],text=True)
assert "name='com.zunevio.music'" in badging and f"versionCode='{code}'" in badging and f"versionName='{version}'" in badging
assert 'application-debuggable' not in badging
name=f'Zunevio-{version}-universal.apk'; shutil.copy2(apk,dist/name)
(dist/'SIGNING-CERTIFICATE.txt').write_text(sig)
(dist/'PACKAGE.txt').write_text(badging)
commit=subprocess.check_output(['git','rev-parse','HEAD'],text=True).strip()
report={'version':version,'versionCode':code,'commit':commit,'tests':total,'passed':total-len(tests)-skipped,'skipped':skipped,'known_network_test_failures':tests,'lint_errors':len(errors),'lint_warnings':sum(x.get('severity')=='Warning' for x in lint),'physical_device_tested':False,'future_update_install_tested_on_device':False,'production_signer_sha256':expected}
(dist/'VALIDATION.json').write_text(json.dumps(report,indent=2)+'\n')
source=dist/f'Zunevio-{version}-corresponding-source.zip'
subprocess.run(['git','archive','--format=zip',f'--prefix=Zunevio-{version}/',f'--output={source}', 'HEAD'],check=True)
with zipfile.ZipFile(source) as z:
    bad=[n for n in z.namelist() if n.endswith(('.jks','.keystore')) or n.split('/')[-1] in ('keystore.properties','local.properties','password.txt')]
    assert not bad, 'Secret files in corresponding source: '+str(bad)
for file in ('LICENSE','NOTICE.md','THIRD_PARTY_NOTICES.md','RELEASE_NOTES.md','BUILDING.md','PRIVACY.md'):
    shutil.copy2(file,dist/file)
with (dist/'SHA256SUMS.txt').open('w') as out:
    for f in sorted(dist.iterdir()):
        if f.is_file() and f.name!='SHA256SUMS.txt': out.write(hashlib.sha256(f.read_bytes()).hexdigest()+'  '+f.name+'\n')
print(json.dumps(report,indent=2))
