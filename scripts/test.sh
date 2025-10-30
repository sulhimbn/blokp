#!/bin/bash
# Test script for IuranKomplek using Docker

echo "Running tests for IuranKomplek..."

# Run unit tests
echo "Running unit tests..."
docker-compose exec android-builder ./gradlew test

# Run instrumented tests (if emulator is available)
echo "Running instrumented tests..."
docker-compose exec android-builder ./gradlew connectedAndroidTest

echo "Tests completed!"