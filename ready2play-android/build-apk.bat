@echo off
title Ready2Play Launcher - Build APK
echo ====================================================
echo      Ready2Play Launcher (Android Edition)
echo                Builder APK
echo ====================================================
echo.

where gradlew >nul 2>nul
if %errorlevel% neq 0 (
    echo [INFO] Memeriksa instalasi Android Studio / Java...
)

if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo File APK sebelumnya ditemukan.
)

echo [1] Buka Android Studio dan pilih folder ini:
echo     %~dp0
echo.
echo [2] Klik menu: Build ^> Build Bundle(s) / APK(s) ^> Build APK(s)
echo.
echo [3] Hasil file .apk akan langsung muncul di:
echo     app\build\outputs\apk\debug\app-debug.apk
echo.
echo ====================================================
pause
