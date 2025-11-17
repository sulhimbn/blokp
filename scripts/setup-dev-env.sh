#!/bin/bash
# Setup lingkungan pengembangan Docker untuk Iuran BlokP

echo "Menyiapkan lingkungan pengembangan Docker untuk Iuran BlokP..."

# Membuat direktori yang diperlukan
mkdir -p mock-api/mock-data
mkdir -p scripts

# Build dan mulai container
echo "Building dan memulai container Docker..."
docker-compose up --build -d

# Tunggu layanan siap
echo "Menunggu layanan siap..."
sleep 10

# Inisialisasi Android SDK
echo "Menginisialisasi Android SDK..."
docker-compose exec android-builder /workspace/gradlew --version

# Unduh komponen Android SDK
echo "Mengunduh komponen Android SDK..."
docker-compose exec android-builder sdkmanager --install "platforms;android-34" "build-tools;34.0.0"

echo "Setup selesai! Anda sekarang dapat:"
echo "1. Mengakses VS Code di http://localhost:8081"
echo "2. Mengakses mock API di http://localhost:8080"
echo "3. Menjalankan build dengan: docker-compose exec android-builder ./gradlew build"