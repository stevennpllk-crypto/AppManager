#!/data/data/com.termux/files/usr/bin/bash
set -e
cd "$(dirname "$0")/.."
echo "=== AppManager mobile build ==="
echo "This script expects Java 17+ and Gradle to be available in Termux."
command -v java >/dev/null || { echo "Java not found. Install a JDK in your Android build environment."; exit 1; }
command -v gradle >/dev/null || { echo "Gradle not found. Install Gradle or build with AndroidIDE."; exit 1; }
java -version
gradle --version | head -12
echo "Building debug APK..."
gradle --no-daemon --stacktrace assembleDebug
APK="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK" ]; then
  echo
  echo "BUILD SUCCESSFUL"
  echo "APK: $PWD/$APK"
else
  echo "Build finished but APK was not found at $APK"
  exit 2
fi
