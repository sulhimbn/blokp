#!/bin/bash
# Script build untuk BlokP menggunakan Docker

echo "Building BlokP dengan Docker..."

# Build aplikasi Android
docker-compose exec android-builder ./gradlew assembleDebug

# Salin APK ke direktori lokal
echo "Menyalin APK ke direktori lokal..."
docker-compose cp android-builder:/workspace/app/app/build/outputs/apk/debug/app-debug.apk .

echo "Build selesai! APK tersedia di: app-debug.apk"