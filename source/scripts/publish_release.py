#!/usr/bin/env python3
"""Stage identical assets on both repos before making either release visible."""
import glob,json,os,subprocess
from pathlib import Path
report=json.loads(Path('dist/VALIDATION.json').read_text()); tag='v'+report['version']
repos=[os.environ['GITHUB_REPOSITORY'],'amitsami/zunevio-releases']
assets=sorted(str(p) for p in Path('dist').iterdir() if p.is_file())
def gh(*args): return subprocess.run(['gh',*args],check=True)
# Never replace a published version (immutable version/signing chain).
for repo in repos:
    result=subprocess.run(['gh','release','view',tag,'-R',repo,'--json','isDraft'],text=True,capture_output=True)
    if result.returncode==0:
        if not json.loads(result.stdout)['isDraft']: raise SystemExit('Version already published: '+repo+' '+tag)
    else:
        gh('release','create',tag,'-R',repo,'--draft','--title','Zunévio '+report['version'],'--notes-file','RELEASE_NOTES.md')
for repo in repos:
    gh('release','upload',tag,*assets,'-R',repo,'--clobber')
    actual=json.loads(subprocess.check_output(['gh','release','view',tag,'-R',repo,'--json','assets'],text=True))['assets']
    mapping={x['name']:x['size'] for x in actual}
    assert all(mapping.get(Path(x).name)==Path(x).stat().st_size for x in assets),'Incomplete release upload'
for repo in repos: gh('release','edit',tag,'-R',repo,'--draft=false','--latest')
print('Published identical verified assets to both repositories.')
