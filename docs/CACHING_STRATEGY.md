# Caching Strategy Architecture

## Overview

The caching strategy implements an offline-first architecture that provides resilient data access with intelligent cache management. The system uses a **cache-first approach** with automatic fallback to network when cache is stale or unavailable.

## Architecture Components

### 1. Cache Manager (Singleton)
**Location**: `data/cache/CacheManager.kt`

**Responsibilities**:
- Initialize and manage Room database instance
- Provide thread-safe access to DAOs
- Manage cache freshness thresholds
- Provide cache clearing operations

**Key Features**:
- Singleton pattern ensures single database instance
- Thread-safe initialization
- Configurable cache freshness threshold (default: 5 minutes)
- Automatic integrity checking on database open

### 2. Cache Strategies
**Location**: `data/cache/CacheStrategies.kt`

#### Cache-First Strategy
```kotlin
cacheFirstStrategy(
    getFromCache = { ... },      // Fetch from database
    getFromNetwork = { ... },    // Fetch from API
    isCacheFresh = { ... },       // Check if data is fresh
    saveToCache = { ... },       // Save API response to database
    forceRefresh = false          // Bypass cache if true
)
```

**Behavior**:
1. Check cache for data
2. If data exists and is fresh, return cached data
3. If data is stale or missing, fetch from network
4. Save network response to cache
5. If network fails, fallback to cached data (even if stale)

**Use Cases**:
- User lists (read-heavy, relatively static)
- Financial records (cached with freshness check)
- Vendor information (rarely changes)

#### Network-First Strategy
```kotlin
networkFirstStrategy(
    getFromNetwork = { ... },    // Fetch from API
    saveToCache = { ... },       // Save API response to database
    getFromCache = { ... }       // Fallback if network fails
)
```

**Behavior**:
1. Attempt to fetch from network
2. Save response to cache
3. If network fails, fallback to cached data

**Use Cases**:
- Transaction data (requires latest state)
- Payment processing (real-time)
- Webhook events (immediate processing)

### 3. Database Preloader
**Location**: `data/cache/DatabasePreloader.kt`

**Responsibilities**:
- Validate indexes on database creation
- Run integrity checks on database open
- Preload frequently accessed data

### 4. Cache Constants
**Location**: `data/cache/CacheConstants.kt`

**Defines**:
- Cache freshness thresholds (short, default, long)
- Maximum cache size limits
- Cache cleanup thresholds
- Cache type identifiers
- Sync status constants

## Cache Integration

### Repository Pattern Integration

#### UserRepository
```kotlin
interface UserRepository {
    suspend fun getUsers(forceRefresh: Boolean = false): Result<UserResponse>
    suspend fun getCachedUsers(): Result<UserResponse>
    suspend fun clearCache(): Result<Unit>
}
```

**Caching Behavior**:
- `getUsers()`: Cache-first strategy with 5-minute freshness
- `getUsers(forceRefresh = true)`: Bypass cache, fetch from API
- `getCachedUsers()`: Return cached data only (no network call)
- `clearCache()`: Clear all user and financial record cache

#### PemanfaatanRepository
```kotlin
interface PemanfaatanRepository {
    suspend fun getPemanfaatan(forceRefresh: Boolean = false): Result<PemanfaatanResponse>
    suspend fun getCachedPemanfaatan(): Result<PemanfaatanResponse>
    suspend fun clearCache(): Result<Unit>
}
```

**Caching Behavior**:
- `getPemanfaatan()`: Cache-first strategy with 5-minute freshness
- `getPemanfaatan(forceRefresh = true)`: Bypass cache, fetch from API
- `getCachedPemanfaatan()`: Return cached data only
- `clearCache()`: Clear financial record cache only

## Data Flow

### Cache-First Flow
```
┌─────────────┐
│ Repository  │
└──────┬──────┘
       │
       │ 1. getData()
       ▼
┌─────────────────────┐
│ Cache-First       │
│ Strategy          │
└──────┬────────────┘
       │
       │ 2. Check cache
       ▼
┌─────────────┐     ┌─────────────┐
│ Database    │     │ Cache       │
│ (Room)      │────▶│ Fresh?      │
└─────────────┘     └──────┬──────┘
                           │
        Yes              │ No
        │                │
        ▼                ▼
┌─────────────┐   ┌─────────────┐
│ Return      │   │ API Service │
│ Cached Data │   │ (Network)   │
└─────────────┘   └──────┬──────┘
                          │
                          │ 3. Save to cache
                          ▼
                   ┌─────────────┐
                   │ Database    │
                   └─────────────┘
```

### Offline Scenario
```
┌─────────────┐
│ Repository  │
└──────┬──────┘
       │
       │ 1. getData()
       ▼
┌─────────────────────┐
│ Cache-First       │
│ Strategy          │
└──────┬────────────┘
       │
       │ 2. Network call FAILS
       ▼
┌─────────────────────┐
│ Fallback to Cache  │
└──────┬────────────┘
       │
       │ 3. Return cached data
       ▼
┌─────────────┐
│ UI shows    │
│ cached data  │
└─────────────┘
```

## Cache Invalidation

### Automatic Invalidation
- **Time-based**: Cache expires after configured freshness threshold
- **Version-based**: Future enhancement with ETag/Last-Modified headers

### Manual Invalidation
```kotlin
// Clear specific cache
repository.clearCache()

// Force refresh on next call
repository.getData(forceRefresh = true)
```

### Data Synchronization
When API data is fetched:
1. Check if user exists by email (unique identifier)
2. If exists: Update record (preserve ID, update timestamps)
3. If not exists: Insert new record
4. Same logic for financial records (by user_id)

## Performance Optimizations

### Indexes
- **Users table**: Email index (unique)
- **Financial records**: user_id index, updated_at DESC index

### Query Efficiency
- Use Flow for reactive queries (auto-updates when data changes)
- Batching operations for bulk inserts/updates
- Prepared statements for frequently executed queries

### Memory Management
- Paging library support (future enhancement)
- LRU cache for in-memory data (future enhancement)
- Automatic cleanup of stale data (future enhancement)

## Testing

### Unit Tests
- `CacheStrategiesTest.kt`: 13 test cases
  - Cache-first strategy scenarios
  - Network-first strategy scenarios
  - Fallback behavior tests
  - Error handling tests

- `CacheManagerTest.kt`: 18 test cases
  - Cache freshness validation
  - Threshold configuration tests
  - CRUD operations tests
  - Query tests
  - Constraint validation tests

### Integration Tests
- Repository tests with caching enabled
- Offline scenario simulation
- Cache synchronization tests

## Best Practices

### When to Use Cache-First
- Read-heavy operations
- Data that changes infrequently
- Lists and catalogs
- User profiles

### When to Use Network-First
- Critical operations requiring latest data
- Transaction processing
- Real-time updates
- Payment operations

### Cache Freshness Thresholds
- **Short (1 minute)**: Real-time data (transactions, payments)
- **Default (5 minutes)**: Standard data (users, financial records)
- **Long (30 minutes)**: Static data (settings, configuration)

## Migration Path

### Existing Code
- No changes required to ViewModels or Activities
- Repository interfaces maintain backward compatibility
- Default behavior is cache-first (5-minute freshness)

### Migration to Cache-First
```kotlin
// Before (API only)
repository.getUsers()

// After (cache-first, same API)
repository.getUsers() // Cache-first with 5min freshness

// Force refresh when needed
repository.getUsers(forceRefresh = true)

// Get cached data only
repository.getCachedUsers()
```

## Future Enhancements

### Phase 2: Advanced Caching
- **Automatic synchronization**: Background sync when online
- **Conflict resolution**: Merge strategy for offline edits
- **Incremental updates**: Fetch only changed data (delta sync)
- **Cache preloading**: Preload data on app startup

### Phase 3: Performance
- **Paging integration**: Efficient large dataset handling
- **In-memory cache**: LRU cache for frequently accessed data
- **Prefetching**: Predictive data loading
- **Compressed cache**: Reduce storage footprint

### Phase 4: Monitoring
- **Cache hit rate analytics**: Track cache effectiveness
- **Performance metrics**: Query execution times
- **Error tracking**: Cache failures and fallbacks
- **Size monitoring**: Track cache storage usage

## Troubleshooting

### Cache Not Updating
**Symptom**: Stale data displayed despite network being available

**Solutions**:
1. Force refresh: `repository.getData(forceRefresh = true)`
2. Check cache freshness threshold
3. Verify network connectivity
4. Clear cache: `repository.clearCache()`

### Database Integrity Issues
**Symptom**: App crashes with database errors

**Solutions**:
1. Check Android logs for SQLite errors
2. Verify migration scripts executed correctly
3. Clear app data to reset database
4. Report bug with stack trace

### High Memory Usage
**Symptom**: App using excessive memory

**Solutions**:
1. Reduce cache freshness threshold
2. Implement periodic cache cleanup
3. Use paging for large datasets
4. Clear unused caches

## Success Criteria

- [x] Cache-first strategy implemented
- [x] Network-first strategy implemented
- [x] CacheManager singleton for database access
- [x] Cache freshness validation
- [x] Offline scenario support
- [x] Repository integration (UserRepository, PemanfaatanRepository)
- [x] Data synchronization (API → Cache)
- [x] Cache invalidation (manual and time-based)
- [x] Database indexes for performance
- [x] Comprehensive unit tests (31 test cases)
- [ ] Automatic background synchronization (future)
- [ ] Conflict resolution for offline edits (future)
- [ ] Incremental sync (delta) (future)

## Conclusion

The caching strategy provides a robust offline-first architecture with intelligent cache management. The system ensures data availability even during network outages while maintaining data freshness through configurable thresholds. The cache-first approach optimizes for performance, reducing network calls and improving user experience.

**Architecture Status**: Production Ready ✅
**Test Coverage**: Comprehensive ✅
**Performance**: Optimized ✅
**Offline Support**: Implemented ✅
