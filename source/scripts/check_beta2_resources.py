from pathlib import Path
import xml.etree.ElementTree as E
import re,json,hashlib
root=Path(__file__).resolve().parents[1]
out=root/'validation/1.9-beta2';out.mkdir(parents=True,exist_ok=True)
checks={}
for module,base in [('android',root/'app/src/main/res'),('compose',root/'sharedUi/src/commonMain/composeResources')]:
 def parse(folder):
  return {e.get('name'): e for e in E.parse(base/folder/'strings.xml').getroot() if e.get('name')}
 en=parse('values')
 for folder in ['values-bn','values-bn-rBD']:
  bn=parse(folder);missing=set(en)-set(bn)
  assert not missing,(module,folder,missing)
  mismatches=[]
  for k,v in en.items():
   pattern=r'%(?:\d+\$)?(?:\.\d+)?[dsf]' # Android UI placeholders, not literal percentages
   # Match each plural quantity / array position independently.
   original=list(v) or [v];translated=list(bn[k]) or [bn[k]]
   if len(original)!=len(translated):mismatches.append(k);continue
   for a,b in zip(original,translated):
    if sorted(re.findall(pattern,''.join(a.itertext())))!=sorted(re.findall(pattern,''.join(b.itertext()))):mismatches.append(k)
  assert not mismatches,(module,folder,mismatches)
  checks[f'{module}_{folder}']={'resource_groups':len(bn),'missing':0,'format_mismatches':0}
home=(root/'sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/screens/HomeScreen.kt').read_text()
library=(root/'sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/screens/LibraryScreen.kt').read_text()
assert not re.search(r'\bSignInBanner\s*\(',home)
assert 'onAction = onSignIn' not in library
picker=(root/'app/src/main/java/com/music/bitchord/ui/components/AppLanguageDialog.kt').read_text()
assert 'AppLanguage("bn-BD"' in picker and 'AppLanguage("hi"' not in picker
checks['home_library_sign_in_ctas_absent']=True
checks['bangla_picker_present_hindi_absent']=True
runtime=[]
for source in [root/'app/src',root/'shared/src',root/'sharedUi/src']:
 for p in source.rglob('*.kt'):
  if 'test' in str(p.relative_to(source)).lower():continue
  for url in re.findall(r'https?://[^\s\)"\'<>]+',p.read_text()):
   if ('bitchord' in url.lower() or 'kushagrasingh' in url.lower()) and not url.startswith('https://github.com/kushagrasinghx/BitChord'):
    runtime.append({'file':str(p.relative_to(root)),'url':url})
checks['upstream_http_reference_scan']=runtime
checks['scope']='Source/static checks only; not a network packet capture, device benchmark or runtime acceptance result.'
(out/'static-validation.json').write_text(json.dumps(checks,ensure_ascii=False,indent=2))
print(json.dumps(checks,ensure_ascii=False,indent=2))
