# Docker Development Environment for Iuran BlokP

This document provides instructions for using the Docker-based development environment for the Iuran BlokP Android application.

## Overview

The Docker environment provides an isolated, consistent, and reproducible development setup for the Iuran BlokP application. It includes:

- Android build container with all necessary SDKs and tools
- Mock API server for offline development
- Development tools container with VS Code Server
- Persistent volumes for caching and data storage

## Prerequisites

Before using the Docker environment, ensure you have:

1. Docker installed on your system
2. Docker Compose installed
3. Git (for cloning the repository)

## Quick Start

### 1. Setup the Environment

Run the setup script to initialize the Docker environment:

```bash
./scripts/setup-dev-env.sh
```

This script will:
- Build and start all Docker containers
- Initialize the Android SDK
- Download necessary Android SDK components
- Display access URLs for services

### 2. Access Development Tools

After setup, you can access:

- **VS Code Web Interface**: http://localhost:8081
- **Mock API Server**: http://localhost:8080

### 3. Build the Application

To build the Android application:

```bash
./scripts/build.sh
```

This will:
- Build the debug APK
- Copy the APK to your local directory

### 4. Run Tests

To run tests:

```bash
./scripts/test.sh
```

This will:
- Run unit tests
- Run instrumented tests (if emulator is available)

## Docker Commands

### Manual Docker Commands

If you prefer to use Docker commands directly:

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Rebuild and restart
docker-compose up --build -d

# Execute a command in the Android builder
docker-compose exec android-builder ./gradlew build

# Access the Android builder shell
docker-compose exec android-builder /bin/bash
```

### Service Management

Each service can be managed individually:

```bash
# Start only the Android builder
docker-compose up android-builder

# Start only the mock API
docker-compose up api-mock

# Start only the development tools
docker-compose up dev-tools
```

## Project Structure

```
IuranBlokP/
```

## Development Workflow

### 1. Initial Setup

1. Clone the repository
2. Run the setup script: `./scripts/setup-dev-env.sh`
3. Access VS Code at http://localhost:8081
4. Configure your IDE to use the Docker-based Android SDK

### 2. Daily Development

1. Code changes are automatically synced to containers
2. Build the app: `./scripts/build.sh`
3. Run tests: `./scripts/test.sh`
4. Access the mock API at http://localhost:8080

### 3. Working with the Mock API

The mock API server simulates the external API used by the application. To modify mock data:

1. Edit files in `mock-api/mock-data/`
2. The changes are automatically reflected in the running container
3. Test your application with the modified mock data

### 4. Building for Production

To create a release APK:

```bash
docker-compose exec android-builder ./gradlew assembleRelease
docker-compose cp android-builder:/workspace/app/app/build/outputs/apk/release/app-release.apk .
```

## Environment Variables

The Docker environment uses several environment variables:

### Docker Environment Variables

- `DOCKER_ENV`: Set to indicate when running in Docker (used by ApiConfig.kt)

### Container-Specific Variables

- `GRADLE_USER_HOME`: Path to Gradle home directory in the Android builder
- `JAVA_HOME`: Path to Java installation
- `ANDROID_SDK_ROOT`: Path to Android SDK installation

## Volume Management

The Docker environment uses several volumes for persistence:

### gradle-cache
- Location: `gradle-cache:/home/gradle/.gradle`
- Purpose: Caches Gradle dependencies between builds
- Benefits: Faster subsequent builds

### android-sdk-cache
- Location: `android-sdk-cache:/opt/android-sdk`
- Purpose: Caches Android SDK components
- Benefits: Faster SDK updates

### Project Source
- Location: `./:/workspace/app`
- Purpose: Mounts the entire project into containers
- Benefits: Real-time code changes

## Networking

Containers are connected to a dedicated Docker network (`iuran-network`) with:

- Internal DNS resolution between containers
- Isolated from host network for security
- Port mappings:
  - API Mock: 8080:5000
  - VS Code: 8081:8080

## Troubleshooting

### Common Issues

1. **Docker not running**
   - Ensure Docker is installed and running
   - Check with `docker --version`

2. **Port conflicts**
   - If ports 8080 or 8081 are in use, modify `docker-compose.yml`
   - Change the host port mappings

3. **Gradle build failures**
   - Clear Gradle cache: `docker volume rm iuran_gradle-cache`
   - Rebuild containers: `docker-compose up --build -d`

4. **Mock API not responding**
   - Check logs: `docker-compose logs api-mock`
   - Verify mock data files are valid JSON

### Debugging Tips

1. **Access container shells**:
   ```bash
   docker-compose exec android-builder /bin/bash
   docker-compose exec api-mock /bin/bash
   ```

2. **View container logs**:
   ```bash
   docker-compose logs -f android-builder
   docker-compose logs -f api-mock
   ```

3. **Check resource usage**:
   ```bash
   docker stats
   ```

## Best Practices

### Development

1. **Use the provided scripts** for common operations
2. **Commit mock data changes** to version control
3. **Regularly update Docker images** for security patches
4. **Clean up unused containers and volumes**:
   ```bash
   docker system prune -f
   ```

### Security

1. **Don't store sensitive data** in Docker images
2. **Use environment variables** for configuration
3. **Limit container privileges** when possible
4. **Keep images updated** with security patches

### Performance

1. **Use volume caching** for Gradle and SDK
2. **Build incrementally** when possible
3. **Use .dockerignore** to reduce image size
4. **Clean up resources** regularly

## CI/CD Integration

The Docker environment is designed for easy integration with CI/CD pipelines:

### Example CI Configuration

```yaml
# .github/workflows/android.yml
name: Android CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2

    - name: Set up Docker
      run: |
        docker-compose up -d
        sleep 30

    - name: Build APK
      run: ./scripts/build.sh

    - name: Run Tests
      run: ./scripts/test.sh

    - name: Upload APK
      uses: actions/upload-artifact@v2
      with:
        name: app-apk
        path: app-debug.apk
```

## Contributing

When contributing to this Docker environment:

1. Test changes thoroughly
2. Update documentation as needed
3. Follow Docker best practices
4. Ensure compatibility with different host systems

## Support

For issues or questions related to the Docker environment:

1. Check this README and troubleshooting section
2. Review container logs
3. Verify Docker and Docker Compose installation
4. Consult Docker documentation

---

*This Docker environment is designed to streamline development and ensure consistency across all team members and deployment environments.*