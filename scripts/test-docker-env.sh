#!/bin/bash
# Test script to verify Docker environment functionality

echo "Testing Docker environment for IuranKomplek..."

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Check if required files exist
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
        echo "❌ Required file not found: $file"
        exit 1
    fi
done

echo "✅ All required files are present"

# Test Docker build
echo "Testing Docker build..."
docker-compose build --no-cache
if [ $? -eq 0 ]; then
    echo "✅ Docker build successful"
else
    echo "❌ Docker build failed"
    exit 1
fi

# Start services
echo "Starting Docker services..."
docker-compose up -d
if [ $? -eq 0 ]; then
    echo "✅ Docker services started successfully"
else
    echo "❌ Failed to start Docker services"
    exit 1
fi

# Wait for services to be ready
echo "Waiting for services to be ready..."
sleep 30

# Check if mock API is responding
echo "Testing mock API..."
if curl -s http://localhost:8080/data/QjX6hB1ST2IDKaxB/ > /dev/null; then
    echo "✅ Mock API is responding"
else
    echo "❌ Mock API is not responding"
    docker-compose logs api-mock
    exit 1
fi

# Test Android builder
echo "Testing Android builder..."
docker-compose exec android-builder /workspace/gradlew --version > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✅ Android builder is working"
else
    echo "❌ Android builder is not working"
    docker-compose logs android-builder
    exit 1
fi

# Test basic Gradle task
echo "Testing Gradle task..."
docker-compose exec android-builder /workspace/gradlew tasks > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✅ Gradle tasks are working"
else
    echo "❌ Gradle tasks are not working"
    exit 1
fi

# Clean up
echo "Cleaning up..."
docker-compose down

echo ""
echo "🎉 Docker environment test completed successfully!"
echo ""
echo "Next steps:"
echo "1. Run './scripts/setup-dev-env.sh' to set up the environment"
echo "2. Access VS Code at http://localhost:8081"
echo "3. Access mock API at http://localhost:8080"
echo "4. Build the app with './scripts/build.sh'"