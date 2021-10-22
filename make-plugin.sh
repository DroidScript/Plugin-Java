

cd app/build/outputs/apk/debug
rm -r temp
unzip app-debug.apk -d temp
cd temp
zip MyPlugin.jar classes.dex
rm classes.dex
cp -r assets/* .
cp -r lib/* .
rm -r lib
rm -r assets
rm -r res
rm AndroidManifest.xml
rm resources.arsc
rm -r META-INF
zip -r ../MyPlugin.ppk *

cd ..
export PATH=$PATH:~/Library/Android/sdk/platform-tools/
adb push MyPlugin.ppk /sdcard/Android/data/com.smartphoneremote.androidscriptfree/files/DroidScript/Plugins/MyPlugin.ppk
