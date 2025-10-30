#!/bin/bash
# Setup Docker development environment for IuranKomplek

echo "Setting up Docker development environment for IuranKomplek..."

# Create necessary directories
mkdir -p mock-api/mock-data
mkdir -p scripts

# Build and start containers
echo "Building and starting Docker containers..."
docker-compose up --build -d

# Wait for services to be ready
echo "Waiting for services to be ready..."
sleep 10

# Initialize Android SDK
echo "Initializing Android SDK..."
docker-compose exec android-builder /workspace/gradlew --version

# Download Android SDK components
echo "Downloading Android SDK components..."
docker-compose exec android-builder sdkmanager --install "platforms;android-34" "build-tools;34.0.0"

echo "Setup complete! You can now:"
echo "1. Access VS Code at http://localhost:8081"
echo "2. Access mock API at http://localhost:8080"
echo "3. Run builds with: docker-compose exec android-builder ./gradlew build"