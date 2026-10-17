import json, pathlib, shutil, re
root=pathlib.Path(__file__).resolve().parents[2]
config=json.loads((root/'.game-apk-builder.json').read_text(encoding='utf-8-sig'))
source=(root/config['gameFolder']).resolve()
if source!=root and root not in source.parents: raise SystemExit('Game folder must be inside repository')
entry=pathlib.PurePosixPath(config['entryFile'])
if '..' in entry.parts or entry.is_absolute() or not str(entry).lower().endswith(('.html','.htm')): raise SystemExit('Invalid HTML entry')
if not (source/str(entry)).is_file(): raise SystemExit('HTML entry not found')
target=root/'.tv-apk/app/src/main/assets/game'
target.mkdir(parents=True,exist_ok=True)
exclude={'.git','.github','.tv-apk','fire-tv','node_modules','.gradle','.idea','.vscode','work','outputs','.next','.cache'}
allowed={'.html','.htm','.js','.mjs','.cjs','.css','.json','.png','.jpg','.jpeg','.gif','.webp','.avif','.svg','.ico','.wasm','.mp3','.ogg','.wav','.m4a','.mp4','.webm','.ttf','.otf','.woff','.woff2','.bin','.data','.glb','.gltf','.ktx','.ktx2','.basis','.txt','.fnt','.xml','.br','.gz','.unityweb'}
count=0
for file in source.rglob('*'):
    relative=file.relative_to(source)
    if set(relative.parts)&exclude or file.is_symlink() or not file.is_file(): continue
    if file.name in {'package.json','package-lock.json','tsconfig.json','.game-apk-builder.json'}: continue
    if file.suffix.lower() not in allowed: continue
    destination=target/relative; destination.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(file,destination);count+=1
if count==0:raise SystemExit('No static game files found')
html=(target/str(entry)).read_text(encoding='utf-8-sig')
bridge='<script src="/__tvbuilder-controller.js"></script>'
if re.search(r'<head\b[^>]*>',html,re.I):html=re.sub(r'(<head\b[^>]*>)',lambda m:m.group(1)+bridge,html,count=1,flags=re.I)
else:html=bridge+html
(target/str(entry)).write_text(html,encoding='utf-8')
shutil.copyfile(root/'.tv-apk/native-controller.js',target/'__tvbuilder-controller.js')
print(f'Packaged {count} static game files from {config["gameFolder"]}')
