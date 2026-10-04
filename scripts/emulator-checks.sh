#!/usr/bin/env bash
set -euo pipefail
mkdir -p app/build/device-evidence
collect_evidence() {
  adb logcat -d -b crash > app/build/device-evidence/crash-log.txt || true
  adb pull /sdcard/Android/data/com.wandermate.app/files/screenshots app/build/device-evidence/ || true
}
trap collect_evidence EXIT
adb logcat -c
./gradlew connectedDebugAndroidTest
