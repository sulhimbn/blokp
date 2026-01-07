# Database Schema Design

## Overview
This document defines the database schema for the IuranKomplek application, designed for future Room database implementation.

## Entities

### 1. UserEntity
Represents a user in the system with their profile information.

**Table Name:** `users`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique user identifier |
| email | TEXT | NOT NULL, UNIQUE | User's email address |
| first_name | TEXT | NOT NULL | User's first name |
| last_name | TEXT | NOT NULL | User's last name |
| alamat | TEXT | NOT NULL | User's address |
| avatar | TEXT | NOT NULL | URL to user's avatar image |
| created_at | INTEGER | NOT NULL | Timestamp when record was created |
| updated_at | INTEGER | NOT NULL | Timestamp when record was last updated |

**Constraints:**
- `email` must be unique (ensures no duplicate users)
- All string fields are NOT NULL (ensures data integrity)
- Email format validation at application level

**Indexes:**
- `idx_users_email` on `email` (for fast user lookup by email)

---

### 2. FinancialRecordEntity
Represents a financial record for a specific user.

**Table Name:** `financial_records`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique financial record identifier |
| user_id | INTEGER | NOT NULL, FOREIGN KEY | Reference to users.id |
| iuran_perwarga | INTEGER | NOT NULL, DEFAULT 0 | Per-warga fee amount |
| jumlah_iuran_bulanan | INTEGER | NOT NULL, DEFAULT 0 | Monthly contribution amount |
| total_iuran_individu | INTEGER | NOT NULL, DEFAULT 0 | Total individual contribution |
| pengeluaran_iuran_warga | INTEGER | NOT NULL, DEFAULT 0 | Total resident expenditures |
| total_iuran_rekap | INTEGER | NOT NULL, DEFAULT 0 | Summary total contribution |
| pemanfaatan_iuran | TEXT | NOT NULL | Description of fund usage |
| created_at | INTEGER | NOT NULL | Timestamp when record was created |
| updated_at | INTEGER | NOT NULL | Timestamp when record was last updated |

**Constraints:**
- `user_id` is a foreign key referencing `users(id)` with ON DELETE CASCADE
- All numeric fields are >= 0 (non-negative)
- All numeric fields are NOT NULL with DEFAULT 0
- `pemanfaatan_iuran` is NOT NULL and not blank

**Indexes:**
- `idx_financial_user_id` on `user_id` (for fast queries of user's financial records)
- `idx_financial_updated_at` on `updated_at` (for retrieving latest records)

---

## Relationships

### One-to-Many: User → Financial Records
A single user can have multiple financial records over time.

```
users (1) ----< (N) financial_records
```

**Foreign Key:**
- `financial_records.user_id` → `users.id`

**Cascade Rules:**
- `ON DELETE CASCADE`: When a user is deleted, all associated financial records are automatically deleted
- `ON UPDATE CASCADE`: When a user's ID changes (unlikely), all financial records are updated

---

## Data Integrity Rules

### UserEntity Validation
1. Email must contain '@' symbol
2. Email cannot be blank
3. First name cannot be blank
4. Last name cannot be blank
5. Address (alamat) cannot be blank
6. Email must be unique across all users

### FinancialRecordEntity Validation
1. user_id must be a valid reference to an existing user
2. All numeric values must be non-negative (>= 0)
3. pemanfaatan_iuran cannot be blank
4. user_id must be positive (> 0)

---

## Index Strategy

### Primary Indexes (Automatic)
- `users.id` (Primary Key)
- `financial_records.id` (Primary Key)

### Secondary Indexes (Performance Optimization)

#### User Entity Indexes
| Index Name | Columns | Purpose |
|------------|---------|---------|
| `idx_users_email` | `email` | Fast user lookup by email (login, profile access) |

#### FinancialRecord Entity Indexes
| Index Name | Columns | Purpose |
|------------|---------|---------|
| `idx_financial_user_id` | `user_id` | Fast retrieval of user's financial history |
| `idx_financial_updated_at` | `updated_at` | Sorting records by recency |

### Composite Indexes (Future Consideration)
If we frequently query for users with specific financial criteria, we may add:
- `(user_id, updated_at DESC)` - For retrieving user's latest financial record

---

## Migration Safety

### Current State (No Database)
- Application currently uses API-only architecture
- No persistence layer exists
- Data is fetched from network on-demand

### Future Migration Path
1. **Phase 1**: Add Room database with above schema
2. **Phase 2**: Implement caching strategy (API → Local → UI)
3. **Phase 3**: Add offline-first capability with conflict resolution

### Migration Principles
- All migrations must be reversible (include down migration)
- Non-destructive migrations: prefer adding columns/tables over modifying
- Data validation at boundaries (before saving to database)
- Batch large operations for performance
- Test with realistic data volumes

---

## Query Patterns

### Common Queries

#### 1. Get all users with their latest financial record
```sql
SELECT u.*, f.*
FROM users u
LEFT JOIN financial_records f ON u.id = f.user_id
WHERE f.id IN (
    SELECT MAX(id) FROM financial_records GROUP BY user_id
)
ORDER BY u.last_name ASC
```

#### 2. Get user by email (login, profile access)
```sql
SELECT * FROM users WHERE email = ? LIMIT 1
```

#### 3. Get user's financial history
```sql
SELECT * FROM financial_records 
WHERE user_id = ? 
ORDER BY updated_at DESC
```

#### 4. Get all users with total contributions above threshold
```sql
SELECT u.*, SUM(f.total_iuran_rekap) as total
FROM users u
JOIN financial_records f ON u.id = f.user_id
GROUP BY u.id
HAVING total > ?
ORDER BY total DESC
```

---

## Future Enhancements

### Potential Additions
1. **Transaction Entity**: Track individual payment transactions
2. **Audit Log Entity**: Track all changes to financial records
3. **Notification Entity**: Store notifications for users
4. **Settings Entity**: User preferences and app settings

### Schema Evolution Considerations
- Use versioning for schema changes
- Implement data migration scripts
- Maintain backward compatibility where possible
- Document breaking changes

---

## Security Considerations

### Data Protection
- Sensitive data (email) stored securely
- No passwords stored in database (auth handled separately)
- Input validation at application level before persistence

### Access Control
- Database access restricted to application layer
- No direct SQL access from UI
- Prepared statements to prevent SQL injection

---

## Performance Guidelines

### Expected Data Volumes
- Users: 100-1,000 residents
- Financial Records: Multiple per user (historical tracking)
- Queries: Mostly read-heavy with occasional writes

### Optimization Strategy
- Index frequently queried columns
- Use pagination for large result sets
- Cache frequently accessed data
- Batch operations for multiple inserts/updates
- Consider denormalization for complex queries if needed

---

*Last Updated: 2025-01-07*
*Schema Version: 1.0*
*Architect: Data Architect Agent*
