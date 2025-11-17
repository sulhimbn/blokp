#!/bin/bash
# Script test untuk memverifikasi fungsionalitas lingkungan Docker

echo "Testing lingkungan Docker untuk IuranKomplek..."

# Periksa apakah Docker terinstal
if ! command -v docker &> /dev/null; then
    echo "❌ Docker tidak terinstal. Silakan instal Docker terlebih dahulu."
    exit 1
fi

# Periksa apakah Docker Compose terinstal
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose tidak terinstal. Silakan instal Docker Compose terlebih dahulu."
    exit 1
fi

# Periksa apakah file yang diperlukan ada
required_files=(
    "Dockerfile.android"
    "docker-compose.yml"
    "mock-api/Dockerfile.mock-api"
    "mock-api/app.py"
    "scripts/setup-dev-env.sh"
    "scripts/build.sh"
    "scripts/test.sh"
)

for file in "${required_files[@]}"; do
    if [ ! -f "$file" ]; then
        echo "❌ File yang diperlukan tidak ditemukan: $file"
        exit 1
    fi
done

echo "✅ Semua file yang diperlukan ada"

# Test Docker build
echo "Testing Docker build..."
docker-compose build --no-cache
if [ $? -eq 0 ]; then
    echo "✅ Docker build berhasil"
else
    echo "❌ Docker build gagal"
    exit 1
fi

# Mulai layanan
echo "Memulai layanan Docker..."
docker-compose up -d
if [ $? -eq 0 ]; then
    echo "✅ Layanan Docker berhasil dimulai"
else
    echo "❌ Gagal memulai layanan Docker"
    exit 1
fi

# Tunggu layanan siap
echo "Menunggu layanan siap..."
sleep 30

# Periksa apakah mock API merespons
echo "Testing mock API..."
if curl -s http://localhost:8080/data/QjX6hB1ST2IDKaxB/ > /dev/null; then
    echo "✅ Mock API merespons"
else
    echo "❌ Mock API tidak merespons"
    docker-compose logs api-mock
    exit 1
fi

# Test Android builder
echo "Testing Android builder..."
docker-compose exec android-builder /workspace/gradlew --version > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✅ Android builder berfungsi"
else
    echo "❌ Android builder tidak berfungsi"
    docker-compose logs android-builder
    exit 1
fi

# Test tugas Gradle dasar
echo "Testing tugas Gradle..."
docker-compose exec android-builder /workspace/gradlew tasks > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✅ Tugas Gradle berfungsi"
else
    echo "❌ Tugas Gradle tidak berfungsi"
    exit 1
fi

# Bersihkan
echo "Membersihkan..."
docker-compose down

echo ""
echo "🎉 Test lingkungan Docker selesai dengan sukses!"
echo ""
echo "Langkah selanjutnya:"
echo "1. Jalankan './scripts/setup-dev-env.sh' untuk menyiapkan lingkungan"
echo "2. Akses VS Code di http://localhost:8081"
echo "3. Akses mock API di http://localhost:8080"
echo "4. Build aplikasi dengan './scripts/build.sh'"