# API Documentation for BlokP Application

## Overview

BlokP application uses RESTful API endpoints to manage user data and financial information for residential complex payment management. This document provides comprehensive API specifications, data models, and integration guidelines.

## Base Configuration

### Environment Switching
The application automatically switches between development and production API endpoints based on build configuration:

```kotlin
// ApiConfig.kt
private const val USE_MOCK_API = BuildConfig.DEBUG || System.getenv("DOCKER_ENV") != null
private const val BASE_URL = if (USE_MOCK_API) {
    "http://api-mock:5000/data/QjX6hB1ST2IDKaxB/"
} else {
    "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/"
}
```

### Security Configuration
- **Certificate Pinning**: Applied for production API only
- **Network Security**: Configured in `network_security_config.xml`
- **Debug Overrides**: Enabled for development environment

## API Endpoints

### 1. Users Endpoint

#### GET /users
Retrieves list of users/warga with their payment information.

**Request:**
```http
GET /data/QjX6hB1ST2IDKaxB/users
Content-Type: application/json
```

**Response:**
```json
{
  "data": [
    {
      "first_name": "John",
      "last_name": "Doe",
      "email": "john.doe@example.com",
      "alamat": "Jl. Example No. 123",
      "iuran_perwarga": 500000,
      "total_iuran_rekap": 1500000,
      "jumlah_iuran_bulanan": 500000,
      "total_iuran_individu": 1500000,
      "pengeluaran_iuran_warga": 200000,
      "pemanfaatan_iuran": "Maintenance gedung",
      "avatar": "https://example.com/avatar.jpg"
    }
  ]
}
```

**Usage in Android:**
```kotlin
// MainActivity.kt
val apiService = ApiConfig.getApiService()
val client = apiService.getUsers()
client.enqueue(object : Callback<UserResponse> {
    override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
        if (response.isSuccessful) {
            val dataArray = response.body()?.data
            adapter.setUsers(dataArray ?: emptyList())
        }
    }
    override fun onFailure(call: Call<UserResponse>, t: Throwable) {
        // Handle error
    }
})
```

### 2. Pemanfaatan (Financial Utilization) Endpoint

#### GET /pemanfaatan
Retrieves financial utilization data for reporting.

**Request:**
```http
GET /data/QjX6hB1ST2IDKaxB/pemanfaatan
Content-Type: application/json
```

**Response:**
```json
{
  "data": [
    {
      "first_name": "John",
      "last_name": "Doe",
      "email": "john.doe@example.com",
      "alamat": "Jl. Example No. 123",
      "iuran_perwarga": 500000,
      "total_iuran_rekap": 1500000,
      "jumlah_iuran_bulanan": 500000,
      "total_iuran_individu": 1500000,
      "pengeluaran_iuran_warga": 200000,
      "pemanfaatan_iuran": "Maintenance gedung",
      "avatar": "https://example.com/avatar.jpg"
    }
  ]
}
```

**Usage in Android:**
```kotlin
// LaporanActivity.kt
val apiService = ApiConfig.getApiService()
val client = apiService.getPemanfaatan()
client.enqueue(object : Callback<PemanfaatanResponse> {
    override fun onResponse(call: Call<PemanfaatanResponse>, response: Response<PemanfaatanResponse>) {
        if (response.isSuccessful) {
            val dataArray = response.body()?.data
            // Process financial calculations
            adapter.setPemanfaatan(dataArray ?: emptyList())
        }
    }
    override fun onFailure(call: Call<PemanfaatanResponse>, t: Throwable) {
        // Handle error
    }
})
```

## Data Models

### DataItem
Core data model representing user and financial information.

```kotlin
data class DataItem(
    val first_name: String,           // User's first name
    val last_name: String,            // User's last name
    val email: String,                // User's email address
    val alamat: String,               // Residential address
    val iuran_perwarga: Int,          // Monthly payment amount per person
    val total_iuran_rekap: Int,       // Total payment recap
    val jumlah_iuran_bulanan: Int,    // Monthly payment total
    val total_iuran_individu: Int,    // Individual total payment
    val pengeluaran_iuran_warga: Int, // Payment expenditures
    val pemanfaatan_iuran: String,    // Payment utilization description
    val avatar: String                // Profile picture URL
)
```

### UserResponse
Response wrapper for users endpoint.

```kotlin
data class UserResponse(val data: List<DataItem>)
```

### PemanfaatanResponse
Response wrapper for pemanfaatan endpoint.

```kotlin
data class PemanfaatanResponse(val data: List<DataItem>)
```

## Financial Calculations

### LaporanActivity Calculation Logic
The application implements specific financial calculations in `LaporanActivity.kt`:

```kotlin
// Calculation logic (lines 57-70)
var totalIuranBulanan = 0
var totalPengeluaran = 0
var totalIuranIndividu = 0

for (dataItem in dataArray) {
    totalIuranBulanan += dataItem.iuran_perwarga
    totalPengeluaran += dataItem.pengeluaran_iuran_warga
    // Special multiplier logic: each individual total is multiplied by 3
    totalIuranIndividu += dataItem.total_iuran_individu * 3
}

var rekapIuran = totalIuranIndividu - totalPengeluaran
```

**Important Notes:**
- The multiplier `* 3` is intentional business logic
- Recap calculation: `rekapIuran = totalIuranIndividu - totalPengeluaran`
- All calculations use Indonesian Rupiah (IDR) values

## Error Handling

### Network Error Handling
The application implements comprehensive error handling with retry logic:

```kotlin
private fun getUser(currentRetryCount: Int = 0) {
    if (!NetworkUtils.isNetworkAvailable(this)) {
        Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()
        return
    }
    
    val apiService = ApiConfig.getApiService()
    val client = apiService.getUsers()
    client.enqueue(object : Callback<UserResponse> {
        override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
            if (response.isSuccessful) {
                // Handle success
            } else {
                if (currentRetryCount < maxRetries) {
                    // Retry with exponential backoff
                    Handler(Looper.getMainLooper()).postDelayed({
                        getUser(currentRetryCount + 1)
                    }, 1000L * (currentRetryCount + 1))
                } else {
                    // Show error after max retries
                }
            }
        }
        
        override fun onFailure(call: Call<UserResponse>, t: Throwable) {
            // Similar retry logic for network failures
        }
    })
}
```

### Common Error Scenarios
1. **No Internet Connection**: Handled with user-friendly toast message
2. **API Server Error**: Automatic retry with exponential backoff (max 3 attempts)
3. **Data Parsing Error**: Graceful fallback with empty data handling
4. **Network Timeout**: Built into Retrofit timeout configuration

## Mock API Development

### Local Development Setup
For local development, the application uses a Flask-based mock API server:

```python
# mock-api/app.py
@app.route('/data/QjX6hB1ST2IDKaxB/users', methods=['GET'])
def get_users():
    try:
        users = load_mock_data('users.json')
        return jsonify(users)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/data/QjX6hB1ST2IDKaxB/pemanfaatan', methods=['GET'])
def get_pemanfaatan():
    try:
        pemanfaatan = load_mock_data('pemanfaatan.json')
        return jsonify(pemanfaatan)
    except Exception as e:
        return jsonify({"error": str(e)}), 500
```

### Mock Data Structure
Mock data files are located in `mock-api/mock-data/`:
- `users.json` - Sample user data for development
- `pemanfaatan.json` - Sample financial data for testing

## Security Considerations

### Certificate Pinning
Production API uses certificate pinning for enhanced security:

```kotlin
private fun getCertificatePinner(): CertificatePinner {
    return CertificatePinner.Builder()
        .add("api.apispreadsheets.com", "sha256/PIdO5FV9mQyEclv5rMC4oGNTya7Q9S5/Sn1KTWpQov0=")
        .build()
}
```

### Network Security Configuration
```xml
<!-- network_security_config.xml -->
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.apispreadsheets.com</domain>
        <pin-set expiration="2026-12-31">
            <pin algorithm="sha256">PIdO5FV9mQyEclv5rMC4oGNTya7Q9S5/Sn1KTWpQov0=</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

## Testing

### Unit Tests
Financial calculation logic is covered by unit tests in `LaporanActivityCalculationTest.kt`:

```kotlin
@Test
fun testTotalIuranIndividuCalculation_accumulatesCorrectly() {
    val testItems = listOf(
        DataItem(iuran_perwarga = 100, total_iuran_individu = 50, pengeluaran_iuran_warga = 25),
        DataItem(iuran_perwarga = 200, total_iuran_individu = 75, pengeluaran_iuran_warga = 30)
    )
    
    // Test calculation logic
    var totalIuranIndividu = 0
    for (dataItem in testItems) {
        totalIuranIndividu += dataItem.total_iuran_individu * 3
    }
    
    assertEquals(375, totalIuranIndividu) // (50*3) + (75*3)
}
```

### Integration Testing
- Mock API server provides consistent test data
- Network conditions can be simulated using debug builds
- Certificate pinning is disabled in debug mode for easier testing

## Best Practices

### API Integration
1. **Always check network connectivity** before making API calls
2. **Implement proper error handling** with user-friendly messages
3. **Use retry logic** for transient network failures
4. **Handle null responses** gracefully
5. **Log errors** for debugging but don't expose sensitive information

### Performance Optimization
1. **Use DiffUtil** for RecyclerView updates to improve performance
2. **Implement caching** for frequently accessed data
3. **Optimize image loading** with Glide and proper placeholder handling
4. **Use pagination** for large datasets (future enhancement)

### Security
1. **Never log sensitive data** (tokens, passwords, personal information)
2. **Use certificate pinning** for production APIs
3. **Implement proper timeout** configurations
4. **Validate and sanitize** all API responses
5. **Keep dependencies updated** for security patches

## Future API Enhancements

### Planned Endpoints
Based on the development roadmap, the following endpoints are planned:

1. **Authentication Endpoints**
   - `POST /auth/login` - User authentication
   - `POST /auth/logout` - Session termination
   - `POST /auth/refresh` - Token refresh

2. **Communication Endpoints**
   - `GET /announcements` - Retrieve announcements
   - `POST /announcements` - Create announcement (admin)
   - `GET /messages` - Retrieve user messages
   - `POST /messages` - Send message

3. **Payment Endpoints**
   - `POST /payments/initiate` - Initialize payment
   - `GET /payments/{id}/status` - Check payment status
   - `POST /payments/{id}/confirm` - Confirm payment

4. **User Management Endpoints**
   - `GET /users/profile` - Get user profile
   - `PUT /users/profile` - Update user profile
   - `GET /users/roles` - Get user roles and permissions

### API Versioning Strategy
Future API versions will follow semantic versioning:
- Current: v1 (implicit)
- Next: v2 with breaking changes
- Backward compatibility maintained when possible

---

## Support and Maintenance

### API Monitoring
- Implement logging for API response times
- Monitor error rates and failure patterns
- Set up alerts for API downtime

### Documentation Updates
- This document should be updated with each API change
- Include example requests and responses
- Document any breaking changes clearly

### Contact Information
For API-related issues or questions:
- Check existing GitHub issues first
- Create new issue with detailed description
- Include error logs and reproduction steps

---

*Last Updated: November 2025*
*API Version: 1.0*
*Next Review: December 2025*