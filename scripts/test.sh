#!/bin/bash
# Script test untuk BlokP menggunakan Docker

echo "Menjalankan test untuk BlokP..."

# Jalankan unit tests
echo "Menjalankan unit tests..."
docker-compose exec android-builder ./gradlew test

# Jalankan instrumented tests (jika emulator tersedia)
echo "Menjalankan instrumented tests..."
docker-compose exec android-builder ./gradlew connectedAndroidTest

echo "Test selesai!"