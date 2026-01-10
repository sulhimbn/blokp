# Database Index Redundancy Analysis

## Overview
This document analyzes the current index state for the IuranKomplek database and identifies redundant indexes that can be safely removed to improve storage efficiency and write performance.

## Users Table Indexes

### Current Indexes (from migrations):

| Index Name | Migration | Columns | WHERE Clause | Status |
|-------------|-----------|---------|---------------|---------|
| idx_users_email | Migration1 | email | none | SUPERSEDED |
| index_users_email | Migration1 | email | none | KEPT |
| idx_users_name_sort | Migration3 | last_name, first_name | none | SUPERSEDED |
| idx_users_not_deleted | Migration5 | is_deleted | is_deleted = 0 | REDUNDANT |
| idx_users_active | Migration7 | is_deleted | is_deleted = 0 | REDUNDANT |
| idx_users_active_updated | Migration7 | updated_at | is_deleted = 0 | SUPERSEDED |
| idx_users_email_active | Migration11 | email | is_deleted = 0 | PRIMARY |
| idx_users_name_sort_active | Migration11 | last_name, first_name | is_deleted = 0 | PRIMARY |
| idx_users_id_active | Migration11 | id | is_deleted = 0 | PRIMARY |
| idx_users_updated_at_active | Migration11 | updated_at | is_deleted = 0 | PRIMARY |

### Analysis:

**Redundant Indexes:**

1. **idx_users_not_deleted** (Migration5)
   - **Issue**: Index on `is_deleted` column with `WHERE is_deleted = 0` clause
   - **Problem**: Queries filtering on `is_deleted = 0` don't benefit from index on `is_deleted` alone
   - **Recommendation**: DROP

2. **idx_users_active** (Migration7)
   - **Issue**: Duplicate of idx_users_not_deleted
   - **Problem**: Same redundant index created again
   - **Recommendation**: DROP

3. **idx_users_active_updated** (Migration7)
   - **Issue**: Partial index on `updated_at` with `WHERE is_deleted = 0`
   - **Problem**: Superseded by idx_users_updated_at_active from Migration11 (same columns + better ordering)
   - **Recommendation**: DROP

**Superseded Indexes:**

4. **idx_users_email** (Migration1 - NON-UNIQUE)
   - **Issue**: Non-unique email index for all records (including deleted)
   - **Problem**: Superseded by idx_users_email_active (unique partial index for active records)
   - **Recommendation**: DROP

5. **idx_users_name_sort** (Migration3)
   - **Issue**: Index on `last_name, first_name` for all records
   - **Problem**: Superseded by idx_users_name_sort_active (same columns + partial WHERE clause)
   - **Recommendation**: DROP

**Indexes to Keep:**

- **idx_users_email_active** (UNIQUE, partial): Primary index for email lookup on active records
- **idx_users_name_sort_active** (partial): Primary index for name sorting on active records
- **idx_users_id_active** (partial): Primary index for ID lookup on active records
- **idx_users_updated_at_active** (partial): Primary index for timestamp queries on active records
- **index_users_email** (UNIQUE): Required for deleted record queries (getUserByEmail, emailExists on deleted records)

### Storage Savings (users table):
- Dropped indexes: 5
- Estimated size reduction: 15-25% of total index storage for users table

---

## Financial Records Table Indexes

### Current Indexes (from migrations):

| Index Name | Migration | Columns | WHERE Clause | Status |
|-------------|-----------|---------|---------------|---------|
| idx_financial_user_id | Migration1 | user_id | none | KEPT |
| idx_financial_updated_at | Migration1 | updated_at DESC | none | SUPERSEDED |
| idx_financial_user_rekap | Migration4 | user_id, total_iuran_rekap | none | KEPT |
| idx_financial_not_deleted | Migration5 | is_deleted | is_deleted = 0 | REDUNDANT |
| idx_financial_active | Migration7 | is_deleted | is_deleted = 0 | REDUNDANT |
| idx_financial_active_user_updated | Migration7 | user_id, updated_at DESC | is_deleted = 0 | SUPERSEDED |
| idx_financial_active | Migration8 | is_deleted | is_deleted = 0 | REDUNDANT |
| idx_financial_active_updated | Migration8 | updated_at DESC | is_deleted = 0 | SUPERSEDED |
| idx_financial_user_updated_active | Migration11 | user_id, updated_at DESC | is_deleted = 0 | PRIMARY |
| idx_financial_id_active | Migration11 | id | is_deleted = 0 | PRIMARY |
| idx_financial_pemanfaatan_active | Migration11 | pemanfaatan_iuran | is_deleted = 0 | PRIMARY |
| idx_financial_updated_desc_active | Migration12 | updated_at DESC | is_deleted = 0 | PRIMARY |

### Analysis:

**Redundant Indexes:**

1. **idx_financial_not_deleted** (Migration5)
   - **Issue**: Index on `is_deleted` column with `WHERE is_deleted = 0` clause
   - **Problem**: Same issue as users table - index on filter column is not useful
   - **Recommendation**: DROP

2. **idx_financial_active** (Migration7)
   - **Issue**: Duplicate of idx_financial_not_deleted
   - **Problem**: Same redundant index created again
   - **Recommendation**: DROP

3. **idx_financial_active** (Migration8)
   - **Issue**: Duplicate again of idx_financial_not_deleted
   - **Problem**: Redundant index created a second time
   - **Recommendation**: DROP

**Superseded Indexes:**

4. **idx_financial_active_user_updated** (Migration7)
   - **Issue**: Partial index on `user_id, updated_at DESC` with `WHERE is_deleted = 0`
   - **Problem**: Superseded by idx_financial_user_updated_active from Migration11 (same columns + more explicit)
   - **Recommendation**: DROP

5. **idx_financial_active_updated** (Migration8)
   - **Issue**: Partial index on `updated_at DESC` with `WHERE is_deleted = 0`
   - **Problem**: Superseded by idx_financial_updated_desc_active from Migration12 (same columns + better version)
   - **Recommendation**: DROP

6. **idx_financial_updated_at** (Migration1)
   - **Issue**: Index on `updated_at DESC` for all records
   - **Problem**: Superseded by idx_financial_updated_desc_active (partial index with WHERE clause)
   - **Recommendation**: DROP

**Indexes to Keep:**

- **idx_financial_user_id**: Required for user lookup queries (including deleted records)
- **idx_financial_user_rekap**: Required for user + rekap queries (including deleted records)
- **idx_financial_user_updated_active**: Primary index for user + timestamp queries on active records
- **idx_financial_id_active**: Primary index for ID lookup on active records
- **idx_financial_pemanfaatan_active**: Primary index for pemanfaatan search queries on active records
- **idx_financial_updated_desc_active**: Primary index for timestamp queries with descending order on active records

### Storage Savings (financial_records table):
- Dropped indexes: 6
- Estimated size reduction: 20-30% of total index storage for financial_records table

---

## Summary

### Redundant Indexes to Drop:
- **Users table:** 5 indexes
- **Financial records table:** 6 indexes
- **Total:** 11 redundant indexes

### Estimated Impact:

| Metric | Improvement |
|---------|-------------|
| Storage Savings | 15-30% reduction in total index storage |
| Write Performance | 15-30% faster INSERT/UPDATE/DELETE operations |
| Query Performance | No degradation (partial indexes are used for active queries) |
| Maintenance Overhead | 15-30% reduction in index maintenance time |

### Migration Required:
- **Migration 16** would drop all identified redundant indexes
- **Down Migration 16Down** would recreate them for backward compatibility
- Both migrations would be safe and reversible (no data loss, only index changes)

---

## Recommendations

### Immediate Actions:
1. Review the redundant indexes with the development team
2. Create Migration16 and Migration16Down to drop redundant indexes
3. Test migration on staging database before production deployment
4. Monitor query performance after dropping indexes to verify no degradation

### Future Considerations:
1. Establish index review process for each new migration
2. Document index usage patterns to prevent future redundancy
3. Consider using index usage statistics to identify unused indexes
4. Periodic index audits to catch redundancy early

---

**Document Version:** 1.0
**Last Updated:** 2026-01-10
**Author:** Data Architect (DA-002)
