#!/usr/bin/env python3
"""Static RC checks; not a native UI/performance acceptance test."""
from pathlib import Path
import re,json,hashlib,xml.etree.ElementTree as E
from PIL import Image
root=Path(__file__).resolve().parents[1]
out=root/'validation/1.9-rc1';out.mkdir(exist_ok=True,parents=True)
identity=(root/'shared/src/jvmSharedMain/kotlin/com/music/bitchord/data/NetworkIdentity.kt').read_text()
agent=re.search(r'USER_AGENT = "([^"]+)"',identity)[1];assert all(32<=ord(c)<=126 for c in agent)
for base in ['app/src/main','shared/src/jvmSharedMain','sharedUi/src/jvmSharedMain']:
 for p in (root/base).rglob('*.kt'):assert 'Zunévio/' not in p.read_text(),str(p)
lyrics=(root/'shared/src/jvmSharedMain/kotlin/com/music/bitchord/data/lyrics/LyricsTranslation.kt').read_text()
assert lyrics.count('val response = translationBodyOrNull {')==3
assert lyrics.count('.header("User-Agent", com.music.bitchord.data.NetworkIdentity.USER_AGENT)')==3
home=(root/'sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/screens/HomeScreen.kt').read_text()
part=home[home.index('private fun HomeTitle('):home.index('internal fun SectionHeader(')]
assert 'text = "Zunévio"' in part and 'painterResource(' not in part
main=(root/'app/src/main/java/com/music/bitchord/MainActivity.kt').read_text();back=main[main.index('onBack = when {'):]
assert back.index('settingsSubScreen != null -> ({ dismissSettingsSubScreen() })')<back.index('showAccountScrobbling ->')
assert 'settingsSubScreen = next.page' in main
images=[]
for density,size in [('mdpi',48),('hdpi',72),('xhdpi',96),('xxhdpi',144),('xxxhdpi',192)]:
 for name in ['ic_launcher','ic_launcher_round']:
  p=root/f'app/src/main/res/mipmap-{density}/{name}.webp';im=Image.open(p).convert('RGBA');assert im.size==(size,size)
  assert all(red==green==blue for red,green,blue,alpha in im.getdata()),str(p)
  images.append({'source':str(p.relative_to(root)),'size':size,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()})
ns='{http://schemas.android.com/apk/res/android}'
for rel in ['app/src/main/res/drawable/ic_launcher_foreground.xml','app/src/main/res/drawable/ic_logo.xml','app/src/main/res/drawable/ic_notification_logo.xml','sharedUi/src/commonMain/composeResources/drawable/ic_logo.xml']:
 t=E.parse(root/rel).getroot();assert len(t.findall('path'))==3
 assert t.findall('path')[0].get(ns+'fillColor')=='#00000000'
background=E.parse(root/'app/src/main/res/drawable/ic_launcher_background.xml').getroot();assert background.find('path').get(ns+'fillColor')=='#FF000000'
result={'network_agent_ascii':True,'translation_request_boundaries':3,'no_accented_display_agent_in_main_sources':True,'home_full_text_wordmark':True,'settings_top_back_closes_overlay_and_flag':True,'legacy_monochrome_icons':images,'vector_literal_path_colours':True,'scope':'Static/resource checks; native phone rendering/taps not tested here.'}
(out/'rc-static-validation.json').write_text(json.dumps(result,indent=2));print('PASS: ASCII agent, three guarded request paths, Home wordmark, Settings back wiring, ten legacy icons and vector geometry.')
