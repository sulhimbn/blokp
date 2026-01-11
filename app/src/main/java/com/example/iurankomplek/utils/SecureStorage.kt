package com.example.iurankomplek.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object SecureStorage {

    private const val PREFS_NAME = "secure_prefs"
    private const val KEY_ALIAS = "master_key"

    private var encryptedPrefs: SharedPreferences? = null

    fun getSharedPreferences(context: Context): SharedPreferences {
        if (encryptedPrefs == null) {
            synchronized(this) {
                if (encryptedPrefs == null) {
                    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

                    encryptedPrefs = EncryptedSharedPreferences.create(
                        PREFS_NAME,
                        masterKeyAlias,
                        context,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )
                }
            }
        }
        return requireNotNull(encryptedPrefs) { "EncryptedSharedPreferences not initialized" }
    }

    fun storeString(context: Context, key: String, value: String?) {
        val prefs = getSharedPreferences(context)
        prefs.edit().putString(key, value).apply()
    }

    fun getString(context: Context, key: String, defaultValue: String? = null): String? {
        val prefs = getSharedPreferences(context)
        return prefs.getString(key, defaultValue)
    }

    fun storeBoolean(context: Context, key: String, value: Boolean) {
        val prefs = getSharedPreferences(context)
        prefs.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        val prefs = getSharedPreferences(context)
        return prefs.getBoolean(key, defaultValue)
    }

    fun storeInt(context: Context, key: String, value: Int) {
        val prefs = getSharedPreferences(context)
        prefs.edit().putInt(key, value).apply()
    }

    fun getInt(context: Context, key: String, defaultValue: Int = 0): Int {
        val prefs = getSharedPreferences(context)
        return prefs.getInt(key, defaultValue)
    }

    fun storeLong(context: Context, key: String, value: Long) {
        val prefs = getSharedPreferences(context)
        prefs.edit().putLong(key, value).apply()
    }

    fun getLong(context: Context, key: String, defaultValue: Long = 0L): Long {
        val prefs = getSharedPreferences(context)
        return prefs.getLong(key, defaultValue)
    }

    fun remove(context: Context, key: String) {
        val prefs = getSharedPreferences(context)
        prefs.edit().remove(key).apply()
    }

    fun clear(context: Context) {
        val prefs = getSharedPreferences(context)
        prefs.edit().clear().apply()
    }

    fun contains(context: Context, key: String): Boolean {
        val prefs = getSharedPreferences(context)
        return prefs.contains(key)
    }

    fun getAll(context: Context): Map<String, *> {
        val prefs = getSharedPreferences(context)
        return prefs.all
    }

    fun initialize(context: Context) {
        try {
            getSharedPreferences(context)
        } catch (e: Exception) {
            throw SecurityException("Failed to initialize secure storage", e)
        }
    }
}
