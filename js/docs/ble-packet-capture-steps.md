# BLE Packet Capture steps for IOS/Android devices

## For IOS devices using Packet Logger

### Installing profile on IOS device
1. Download profile for IOS on your Mobile browser https://developer.apple.com/bug-reporting/profiles-and-logs/?name=bluetooth
2. Post download, from Settings, install the profile with on screen instructions

### Setting up Packet logger on Mac
1. Download additional tools for xcode 14.1 - https://developer.apple.com/download/all/?q=xcode
2. Double click and Open the DMG and navigate to Hardware > Packet Logger
3. Provide necessary permissions
4. Start Capture from packet logger
5. You can start capture of IOS device using File > New iOS Trace

Note: Optionally, you can download Mac OS profile and install it from System Preferences > Profiles > Install

While Packet logger provides good packet capture details. You can always export the capture to be viewed in Wireshark.

1. Download Wireshark
2. Save Packet logger capture with .pklg extension
3. Open this file in Wireshark to look at capture

## For Android devices

1. Enable developer options on the Android device settings
2. Enable Bluetooth HCI snoop log option from Developer options
3. Enable USB debugging
4. Restart Bluetooth on phone
5. Perform the activity on Android phones for which bluetooth capture is required
6. Find adb from your ANDROID_HOME directory
7. If your device is properly connected via cable, adb devices should give list of devices
8. Run -> ${ANDROID_HOME}/platform-tools/adb bugreport report
    - If there are multiple devices connected. We identify specific device using adb devices -l and run the above command with -s <device ID> to get bug report for that particular device.
9. unzip report.zip
10. Find the BLE capture file inside the extracted content using a find command from terminal. This will give you possible location for snoop log.
```
find . -name “*bluetooth*”
find . -name “BT_*”
find . -name “*snoop*”
find . -name “*.cfa”
find . -name “*.curf”
```

You may find the snoop log which looks like this -> bluetooth/logs/BT_HCI_2022_1129_120043.cfa.curf

11. Open this file in Wireshark to look at capture




