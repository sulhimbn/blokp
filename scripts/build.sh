#!/bin/bash
# Build script for IuranKomplek using Docker

echo "Building IuranKomplek with Docker..."

# Build the Android app
docker-compose exec android-builder ./gradlew assembleDebug

# Copy the APK to local directory
echo "Copying APK to local directory..."
docker-compose cp android-builder:/workspace/app/app/build/outputs/apk/debug/app-debug.apk .

echo "Build complete! APK available at: app-debug.apk"