package com.example.iurankomplek.transaction

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom

@Database(
    entities = [Transaction::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TransactionDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: TransactionDatabase? = null

        private const val PREFS_NAME = "transaction_db_secure_prefs"
        private const val KEY_DB_PASSPHRASE = "db_encryption_passphrase"
        private const val PASSPHRASE_LENGTH = 32

        fun getDatabase(context: Context): TransactionDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase = getOrCreatePassphrase(context)
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TransactionDatabase::class.java,
                    "transaction_database"
                )
                    .openHelperFactory(factory)
                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * Gets existing passphrase from secure storage or generates a new one.
         * Uses AndroidKeyStore-backed EncryptedSharedPreferences for secure storage.
         */
        private fun getOrCreatePassphrase(context: Context): ByteArray {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val securePrefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            val existingPassphrase = securePrefs.getString(KEY_DB_PASSPHRASE, null)

            return if (existingPassphrase != null) {
                // Decode from Base64 for raw bytes
                android.util.Base64.decode(existingPassphrase, android.util.Base64.NO_WRAP)
            } else {
                val newPassphrase = generateSecurePassphrase()
                // Encode to Base64 for storage
                val encodedPassphrase = android.util.Base64.encodeToString(
                    newPassphrase, 
                    android.util.Base64.NO_WRAP
                )
                securePrefs.edit()
                    .putString(KEY_DB_PASSPHRASE, encodedPassphrase)
                    .apply()
                newPassphrase
            }
        }

        /**
         * Generates a cryptographically secure passphrase using SecureRandom.
         * Returns raw 256-bit (32 bytes) entropy for maximum security.
         */
        private fun generateSecurePassphrase(): ByteArray {
            val random = SecureRandom()
            val passphrase = ByteArray(PASSPHRASE_LENGTH)
            random.nextBytes(passphrase)
            return passphrase
        }
    }
}

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentStatus
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom

@Database(
    entities = [Transaction::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TransactionDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: TransactionDatabase? = null

        private const val PREFS_NAME = "transaction_db_secure_prefs"
        private const val KEY_DB_PASSPHRASE = "db_encryption_passphrase"
        private const val PASSPHRASE_LENGTH = 32

        fun getDatabase(context: Context): TransactionDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase = getOrCreatePassphrase(context)
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TransactionDatabase::class.java,
                    "transaction_database"
                )
                    .openHelperFactory(factory)
                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * Gets existing passphrase from secure storage or generates a new one.
         * Uses AndroidKeyStore-backed EncryptedSharedPreferences for secure storage.
         */
        private fun getOrCreatePassphrase(context: Context): ByteArray {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val securePrefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            val existingPassphrase = securePrefs.getString(KEY_DB_PASSPHRASE, null)

            return if (existingPassphrase != null) {
                existingPassphrase.toByteArray(Charsets.UTF_8)
            } else {
                val newPassphrase = generateSecurePassphrase()
                securePrefs.edit()
                    .putString(KEY_DB_PASSPHRASE, String(newPassphrase, Charsets.UTF_8))
                    .apply()
                newPassphrase
            }
        }

        /**
         * Generates a cryptographically secure passphrase using SecureRandom.
         */
        private fun generateSecurePassphrase(): ByteArray {
            val random = SecureRandom()
            val passphrase = ByteArray(PASSPHRASE_LENGTH)
            random.nextBytes(passphrase)
            // Convert to printable characters for SQLCipher compatibility
            return passphrase.map { (it % 93 + 33).toByte() }.toByteArray()
        }
    }
}
