# Troubleshooting Guide

## Overview

This guide provides solutions to common issues encountered during development, testing, and deployment of the IuranKomplek Android application.

## Development Environment Issues

### Android Studio Setup

#### Issue: Gradle Sync Failed
**Symptoms**: Gradle sync fails with dependency resolution errors
**Solutions**:
```bash
# 1. Check internet connection
ping google.com

# 2. Clear Gradle cache
./gradlew clean
rm -rf .gradle
./gradlew build --refresh-dependencies

# 3. Check Gradle wrapper version
gradle wrapper --gradle-version 7.4

# 4. Update Android Studio
Help → Check for Updates
```

#### Issue: Emulator Not Starting
**Symptoms**: Android emulator fails to start or crashes
**Solutions**:
```bash
# 1. Check system requirements
# - Intel HAXM or AMD Hyper-V installed
# - Virtualization enabled in BIOS

# 2. Cold boot emulator
# Android Studio → AVD Manager → Cold Boot Now

# 3. Wipe emulator data
# Android Studio → AVD Manager → Wipe Data

# 4. Create new AVD with different system image
```

#### Issue: Build Configuration Errors
**Symptoms**: Compilation errors related to SDK versions
**Solutions**:
```gradle
// Check app/build.gradle
android {
    compileSdk 34
    defaultConfig {
        minSdk 24
        targetSdk 34
    }
}

// Check build.gradle (project level)
plugins {
    id 'com.android.application' version '7.3.0' apply false
    id 'org.jetbrains.kotlin.android' version '1.7.10' apply false
}
```

## Docker Development Issues

### Mock API Container

#### Issue: API Mock Not Accessible
**Symptoms**: Connection refused when accessing mock API
**Solutions**:
```bash
# 1. Check container status
docker ps | grep api-mock

# 2. Check container logs
docker logs iuran-api-mock

# 3. Restart container
docker-compose restart api-mock

# 4. Rebuild container
docker-compose up --build api-mock

# 5. Check network connectivity
docker network ls
docker network inspect blokp_iuran-network
```

#### Issue: Port Conflicts
**Symptoms**: Port 8080 already in use
**Solutions**:
```bash
# 1. Find process using port
lsof -i :8080
netstat -tulpn | grep :8080

# 2. Kill conflicting process
kill -9 <PID>

# 3. Change port in docker-compose.yml
ports:
  - "8081:5000"  # Use different host port
```

#### Issue: Volume Mounting Problems
**Symptoms**: Changes to mock data not reflected
**Solutions**:
```bash
# 1. Check volume mounts
docker volume ls
docker volume inspect blokp_gradle-cache

# 2. Rebuild with clean volumes
docker-compose down -v
docker-compose up --build

# 3. Check file permissions
ls -la mock-api/mock-data/
chmod 644 mock-api/mock-data/*.json
```

## Application Runtime Issues

### Network Connectivity

#### Issue: API Connection Failed
**Symptoms**: "Failed to retrieve data" toast message
**Debugging Steps**:
```kotlin
// Add logging to ApiConfig
private fun getApiService(): ApiService {
    val baseUrl = getBaseUrl()
    Log.d("ApiConfig", "Using base URL: $baseUrl")
    
    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()
    
    val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    return retrofit.create(ApiService::class.java)
}
```

**Solutions**:
```bash
# 1. Test API endpoint directly
curl -v "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/"

# 2. Check network permissions in AndroidManifest.xml
<uses-permission android:name="android.permission.INTERNET" />

# 3. Test with mock API
curl -v "http://localhost:8080/data/QjX6hB1ST2IDKaxB/"

# 4. Check firewall/proxy settings
```

#### Issue: SSL Certificate Errors
**Symptoms**: SSL handshake failed, certificate errors
**Solutions**:
```xml
<!-- Add network security config -->
<application
    android:networkSecurityConfig="@xml/network_security_config">
```

```xml
<!-- res/xml/network_security_config.xml -->
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <debug-overrides>
        <trust-anchors>
            <certificates src="system" />
            <certificates src="user" />
        </trust-anchors>
    </debug-overrides>
</network-security-config>
```

### UI/UX Issues

#### Issue: RecyclerView Not Updating
**Symptoms**: Data changes not reflected in UI
**Debugging**:
```kotlin
// Add logging to adapter
fun setUsers(users: List<DataItem>) {
    Log.d("UserAdapter", "Setting ${users.size} users")
    this.users.clear()
    this.users.addAll(users)
    notifyDataSetChanged()
    Log.d("UserAdapter", "Adapter updated with ${this.users.size} users")
}
```

**Solutions**:
```kotlin
// 1. Check if adapter is set
if (rv_users.adapter == null) {
    rv_users.adapter = UserAdapter(mutableListOf())
}

// 2. Run on UI thread
runOnUiThread {
    adapter.setUsers(dataArray)
}

// 3. Check data validity
if (dataArray.isNullOrEmpty()) {
    showEmptyState()
    return
}
```

#### Issue: Images Not Loading
**Symptoms**: Avatar images not displaying
**Solutions**:
```kotlin
// Check Glide configuration
Glide.with(holder.itemView.context)
    .load(user.avatar)
    .apply(RequestOptions()
        .placeholder(R.drawable.icon_avatar)
        .error(R.drawable.icon_avatar)
        .override(80, 80))
    .transform(CircleCrop())
    .into(holder.tvAvatar)

// Add logging
Log.d("Glide", "Loading image: ${user.avatar}")
```

```xml
<!-- Check internet permission -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- Check if image URLs are valid -->
<!-- Test URL in browser -->
```

### Performance Issues

#### Issue: App Crashes on Large Datasets
**Symptoms**: OutOfMemoryError with large user lists
**Solutions**:
```kotlin
// 1. Implement pagination
private var currentPage = 1
private val pageSize = 20

private fun loadUsers(page: Int = 1) {
    // Load users for specific page
}

// 2. Optimize images
Glide.with(context)
    .load(user.avatar)
    .override(80, 80) // Limit image size
    .diskCacheStrategy(DiskCacheStrategy.ALL)
    .into(imageView)

// 3. Use DiffUtil instead of notifyDataSetChanged
class UserAdapter : ListAdapter<DataItem, UserAdapter.ListViewHolder>(DiffCallback) {
    // Implementation
}
```

#### Issue: Slow API Response
**Symptoms**: App freezes during API calls
**Solutions**:
```kotlin
// 1. Add timeout
val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

// 2. Add loading indicator
private fun showLoading() {
    progressBar.visibility = View.VISIBLE
    recyclerView.visibility = View.GONE
}

private fun hideLoading() {
    progressBar.visibility = View.GONE
    recyclerView.visibility = View.VISIBLE
}

// 3. Use coroutines (future improvement)
```

## Testing Issues

### Unit Test Failures

#### Issue: Test Cannot Find Classes
**Symptoms**: NoClassDefFoundError in tests
**Solutions**:
```gradle
// Check test dependencies in app/build.gradle
testImplementation 'junit:junit:4.13.2'
testImplementation 'org.mockito:mockito-core:4.6.1'
testImplementation 'androidx.arch.core:core-testing:2.1.0'

// Ensure test source set is correct
app/src/test/java/com/example/iurankomplek/
```

#### Issue: Mock API Not Working in Tests
**Symptoms**: Tests fail with network errors
**Solutions**:
```kotlin
// Use MockWebServer for testing
@RunWith(AndroidJUnit4::class)
class ApiTest {
    private lateinit var mockServer: MockWebServer
    
    @Before
    fun setup() {
        mockServer = MockWebServer()
        mockServer.start()
    }
    
    @After
    fun tearDown() {
        mockServer.shutdown()
    }
    
    @Test
    fun testApiCall() {
        // Mock response
        mockServer.enqueue(MockResponse()
            .setBody("""{"data":[{"first_name":"Test"}]}""")
            .addHeader("Content-Type", "application/json"))
        
        // Test with mock server URL
        val baseUrl = mockServer.url("/").toString()
        // ... test implementation
    }
}
```

### Instrumented Test Issues

#### Issue: Tests Fail on Emulator
**Symptoms**: Instrumented tests timeout or crash
**Solutions**:
```bash
# 1. Check emulator specifications
# - Use API level 29+ for better test support
# - Ensure sufficient RAM (2GB+)

# 2. Increase test timeout
./gradlew connectedAndroidTest --info

# 3. Run specific test class
./gradlew connectedAndroidTest --tests "com.example.iurankomplek.ExampleInstrumentedTest"

# 4. Check test logs
adb logcat | grep "TestRunner"
```

## Build and Deployment Issues

### APK Generation

#### Issue: Release Build Fails
**Symptoms**: Release APK generation fails with ProGuard errors
**Solutions**:
```gradle
// 1. Disable ProGuard temporarily
buildTypes {
    release {
        minifyEnabled false
        // proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}

// 2. Add ProGuard rules for networking
# In proguard-rules.pro
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
```

#### Issue: Signing Configuration
**Symptoms**: Release APK not signed properly
**Solutions**:
```gradle
// Create keystore
keytool -genkey -v -keystore release-key.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias release

// Add signing config
android {
    signingConfigs {
        release {
            storeFile file('release-key.keystore')
            storePassword 'your-store-password'
            keyAlias 'release'
            keyPassword 'your-key-password'
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
        }
    }
}
```

## Debugging Tools and Techniques

### Logging

#### Application Logging
```kotlin
// Use proper logging levels
Log.v(TAG, "Verbose message")     // Detailed information
Log.d(TAG, "Debug message")      // Debug information
Log.i(TAG, "Info message")       // General information
Log.w(TAG, "Warning message")    // Warning
Log.e(TAG, "Error message", t)   // Error with exception

// Never log sensitive information
Log.d(TAG, "User email: ${user.email}") // ❌ BAD
Log.d(TAG, "User ID: ${user.id}")       // ✅ GOOD
```

#### Network Logging
```kotlin
// Enable network logging in debug builds
if (BuildConfig.DEBUG) {
    val logging = HttpLoggingInterceptor()
    logging.setLevel(HttpLoggingInterceptor.Level.BODY)
    
    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()
}
```

### Debugging Tools

#### Android Studio Debugger
1. **Breakpoints**: Set breakpoints at critical points
2. **Variable Inspection**: Monitor variable values
3. **Step Through**: Execute code line by line
4. **Expression Evaluation**: Test expressions in context

#### Network Inspection
```bash
# Use Chucker for network debugging (debug builds)
debugImplementation "com.github.chuckerteam.chucker:library:3.3.0"

# Access Chucker UI from notification shade
```

#### Memory Profiler
1. Open Android Studio Profiler
2. Select MEMORY tab
3. Record memory usage
4. Analyze memory leaks

## Common Error Messages and Solutions

### Compilation Errors

#### "Cannot resolve symbol 'R'"
**Causes**: Resource file errors, import issues
**Solutions**:
```bash
# 1. Clean and rebuild
./gradlew clean build

# 2. Check resource files
# - Ensure all XML files are valid
# - Check for duplicate resource names

# 3. Invalidate caches
File → Invalidate Caches / Restart
```

#### "Duplicate class" errors
**Causes**: Dependency conflicts
**Solutions**:
```gradle
// Exclude conflicting modules
implementation("com.squareup.retrofit2:retrofit:2.9.0") {
    exclude group: 'com.squareup.okhttp3', module: 'okhttp'
}

// Force specific versions
configurations.all {
    resolutionStrategy.force 'com.squareup.okhttp3:okhttp:4.12.0'
}
```

### Runtime Errors

#### "NetworkOnMainThreadException"
**Causes**: Network operations on main thread
**Solutions**:
```kotlin
// Use enqueue for async operations
client.enqueue(object : Callback<ResponseUser> {
    // Handle response on background thread
})

// Or use coroutines (future improvement)
```

#### "JsonSyntaxException"
**Causes**: JSON parsing errors
**Solutions**:
```kotlin
// Add null safety
val data = response.body()?.data ?: emptyList()

// Validate JSON structure
// Use online JSON validator
// Add error handling for malformed JSON
```

## Performance Monitoring

### Key Metrics to Monitor
1. **App Startup Time**: Should be < 3 seconds
2. **Memory Usage**: Monitor for leaks
3. **Network Latency**: API response times
4. **UI Performance**: Frame rate and jank
5. **Battery Usage**: Background processing

### Monitoring Tools
```kotlin
// Add performance monitoring
// Use Android Studio Profiler
// Monitor Firebase Performance (if implemented)
// Check battery usage stats
```

## Getting Help

### Internal Resources
1. **Documentation**: Check `docs/` folder
2. **Code Comments**: Read inline documentation
3. **Git History**: Check commit messages for context
4. **Team Communication**: Ask in team channels

### External Resources
1. **Android Developer Documentation**: https://developer.android.com/
2. **Stack Overflow**: Search for specific error messages
3. **GitHub Issues**: Check library repositories
4. **Kotlin Documentation**: https://kotlinlang.org/docs/

### Creating Bug Reports
When reporting issues, include:
1. **Device/Emulator specs**: Android version, API level
2. **App version**: Build number and version
3. **Steps to reproduce**: Detailed reproduction steps
4. **Expected vs Actual**: What should happen vs what happens
5. **Logs**: Relevant logcat output
6. **Screenshots**: Visual evidence if applicable

---

*This guide should be updated as new issues are discovered and solutions are found.*