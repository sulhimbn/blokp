package com.example.iurankomplek.data.cache

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DatabasePreloader(private val scope: CoroutineScope) : RoomDatabase.Callback() {
    
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        scope.launch {
            preloadIndexesAndConstraints(db)
        }
    }
    
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        scope.launch {
            validateCacheIntegrity(db)
        }
    }
    
    private suspend fun preloadIndexesAndConstraints(db: SupportSQLiteDatabase) {
        try {
            db.query("PRAGMA index_list('users')").use { cursor ->
                if (cursor.count == 0) {
                    db.execSQL("CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)")
                }
            }
            
            db.query("PRAGMA index_list('financial_records')").use { cursor ->
                if (cursor.count == 0) {
                    db.execSQL("CREATE INDEX IF NOT EXISTS idx_financial_user_id ON financial_records(user_id)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS idx_financial_updated_at ON financial_records(updated_at DESC)")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DatabasePreloader", "Error preloading indexes")
        }
    }
    
    private suspend fun validateCacheIntegrity(db: SupportSQLiteDatabase) {
        try {
            db.query("PRAGMA integrity_check").use { cursor ->
                if (cursor.moveToFirst()) {
                    val result = cursor.getString(0)
                    if (result != "ok") {
                        android.util.Log.w("DatabasePreloader", "Database integrity check: $result")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DatabasePreloader", "Error validating cache integrity")
        }
    }
}
