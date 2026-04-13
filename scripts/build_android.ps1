Param(
  [string]$ProjectPath = "D:\Dev\langmaster"
)

$ErrorActionPreference = "Stop"
Set-Location $ProjectPath

if (!(Test-Path ".\gradlew")) {
  throw "Gradle wrapper missing. Build from Android Studio or add wrapper before CLI build."
}

.\gradlew :app:assembleDebug
Write-Host "APK path: $ProjectPath\app\build\outputs\apk\debug\app-debug.apk"
