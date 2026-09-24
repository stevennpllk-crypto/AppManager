from pathlib import Path
import xml.etree.ElementTree as ET
files = [
    'app/src/main/AndroidManifest.xml',
    'app/src/main/res/values/strings.xml',
    'app/src/main/res/values-ar/strings.xml',
    'app/src/main/res/values/styles.xml',
    'app/src/main/res/drawable/ic_app_manager.xml',
]
for name in files:
    ET.parse(name)
    print('OK', name)
source = Path('app/src/main/java/com/example/appmanager/MainActivity.kt').read_text()
required = ['class MainActivity', 'class AppManagerViewModel', 'fun Dashboard', 'fun AppsScreen', 'fun PermissionsScreen']
for token in required:
    assert token in source, token
print('OK Kotlin structure')
