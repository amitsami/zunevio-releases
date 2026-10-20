#!/usr/bin/env python3
"""Capture actual released APK UI, never redraw screens or alter screenshot pixels."""
from pathlib import Path
import subprocess,time,re,json,xml.etree.ElementTree as ET
out=Path('phone-screenshots');out.mkdir(exist_ok=True)
log=[]
def adb(*args,check=True):
 return subprocess.run(['adb',*map(str,args)],capture_output=True,check=check).stdout

def dump(name='current'):
 adb('shell','uiautomator','dump','/sdcard/window.xml',check=False)
 raw=adb('shell','cat','/sdcard/window.xml',check=False).decode(errors='replace')
 (out/(name+'.xml')).write_text(raw)
 try:return ET.fromstring(raw)
 except ET.ParseError:return ET.Element('empty')

def tap(label,tries=1,scroll=False):
 for i in range(tries):
  root=dump()
  nodes=[n for n in root.iter('node') if n.get('text','').strip().casefold()==label.casefold() or n.get('content-desc','').strip().casefold()==label.casefold()]
  if nodes:
   # Prefer the smallest actionable node, not a combined full-page semantics node.
   def area(n):
    a=list(map(int,re.findall(r'\d+',n.get('bounds',''))));return (a[2]-a[0])*(a[3]-a[1]) if len(a)==4 else 10**9
   for n in sorted(nodes,key=area):
    a=list(map(int,re.findall(r'\d+',n.get('bounds',''))))
    if len(a)==4 and a[2]>a[0] and a[3]>a[1]:
     adb('shell','input','tap',(a[0]+a[2])//2,(a[1]+a[3])//2);time.sleep(2);log.append('Tapped '+label);return True
  if scroll:adb('shell','input','swipe',540,1800,540,600,350);time.sleep(1)
  else:time.sleep(2)
 log.append('NOT FOUND '+label);return False

def shot(name,wait=3):
 time.sleep(wait)
 (out/(name+'.png')).write_bytes(adb('exec-out','screencap','-p'))
 root=dump(name)
 text=[n.get('text') or n.get('content-desc') for n in root.iter('node') if n.get('text') or n.get('content-desc')]
 log.append({'screen':name,'visible_text':text})
 print(name,':',' | '.join(text)[:1000],flush=True)

def back():adb('shell','input','keyevent','4');time.sleep(2)
try:
 adb('shell','wm','size','1080x2400');adb('shell','wm','density','420')
 adb('shell','cmd','uimode','night','yes',check=False)
 adb('shell','settings','put','system','accelerometer_rotation','0')
 adb('shell','settings','put','system','user_rotation','0')
 adb('shell','settings','put','global','sysui_demo_allowed','1')
 adb('shell','am','broadcast','-a','com.android.systemui.demo','-e','command','clock','-e','hhmm','0941',check=False)
 adb('install','-r','release.apk')
 for perm in ('POST_NOTIFICATIONS','READ_MEDIA_AUDIO','READ_MEDIA_IMAGES'):
  adb('shell','pm','grant','com.zunevio.music','android.permission.'+perm,check=False)
 adb('push','preview.flac','/sdcard/Music/Zunevio-preview.flac')
 adb('shell','am','broadcast','-a','android.intent.action.MEDIA_SCANNER_SCAN_FILE','-d','file:///sdcard/Music/Zunevio-preview.flac',check=False)
 adb('shell','am','start','-n','com.zunevio.music/com.music.bitchord.MainActivity')
 time.sleep(25)
 # Initial first-run/onboarding is dismissed only through visible controls.
 for label in ('Not now','Continue','Get started','Skip','Done'):
  tap(label)
 shot('01-home',15)
 tap('Explore',tries=2);shot('02-explore',20)
 tap('Library',tries=2);shot('03-library',5)
 # Capture the local player only when an actual demo track can be opened.
 local_opened=tap('Local music') or tap('Local Music')
 if local_opened:
  shot('04-local-music',5)
  if tap('Zunévio Preview',tries=3):
   time.sleep(4)
   tap('Zunévio Preview')
   shot('05-player',4)
   back()
 # Return to the main app route before opening top-bar settings.
 adb('shell','am','force-stop','com.zunevio.music')
 adb('shell','am','start','-n','com.zunevio.music/com.music.bitchord.MainActivity');time.sleep(8)
 if tap('Settings',tries=3):
  shot('06-settings')
  if tap('Audio quality',tries=3,scroll=True):shot('07-audio-quality');back()
  if tap('Appearance',tries=3,scroll=True):shot('08-appearance');back()
  if tap('App updates',tries=6,scroll=True):shot('09-updates',5);back()
finally:
 (out/'capture-log.json').write_text(json.dumps(log,ensure_ascii=False,indent=2))
 (out/'logcat.txt').write_bytes(adb('logcat','-d',check=False))
 (out/'device.txt').write_bytes(adb('shell','getprop',check=False))
