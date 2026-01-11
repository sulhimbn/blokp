package com.example.iurankomplek.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class SecureStorageTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        SecureStorage.clear(context)
    }

    @After
    fun tearDown() {
        SecureStorage.clear(context)
    }

    @Test
    fun `initialize should create encrypted SharedPreferences`() {
        SecureStorage.initialize(context)

        val prefs = SecureStorage.getSharedPreferences(context)
        assertNotNull(prefs)
    }

    @Test
    fun `initialize should throw SecurityException on failure`() {
        SecureStorage.clear(context)

        val prefs = SecureStorage.getSharedPreferences(context)
        assertNotNull(prefs)
    }

    @Test
    fun `storeString should store and retrieve value correctly`() {
        val key = "test_string_key"
        val value = "test_string_value"

        SecureStorage.storeString(context, key, value)
        val retrieved = SecureStorage.getString(context, key)

        assertEquals(value, retrieved)
    }

    @Test
    fun `storeString with null should remove the key`() {
        val key = "test_null_key"
        SecureStorage.storeString(context, key, "initial_value")

        SecureStorage.storeString(context, key, null)
        val retrieved = SecureStorage.getString(context, key)

        assertNull(retrieved)
    }

    @Test
    fun `getString with missing key should return default value`() {
        val key = "missing_key"
        val defaultValue = "default_value"

        val retrieved = SecureStorage.getString(context, key, defaultValue)

        assertEquals(defaultValue, retrieved)
    }

    @Test
    fun `getString with missing key and no default should return null`() {
        val key = "missing_key"

        val retrieved = SecureStorage.getString(context, key)

        assertNull(retrieved)
    }

    @Test
    fun `storeBoolean should store and retrieve value correctly`() {
        val key = "test_boolean_key"
        val value = true

        SecureStorage.storeBoolean(context, key, value)
        val retrieved = SecureStorage.getBoolean(context, key)

        assertEquals(value, retrieved)
    }

    @Test
    fun `storeBoolean should handle false values`() {
        val key = "test_false_key"
        val value = false

        SecureStorage.storeBoolean(context, key, value)
        val retrieved = SecureStorage.getBoolean(context, key)

        assertEquals(value, retrieved)
    }

    @Test
    fun `getBoolean with missing key should return default value`() {
        val key = "missing_bool_key"
        val defaultValue = true

        val retrieved = SecureStorage.getBoolean(context, key, defaultValue)

        assertEquals(defaultValue, retrieved)
    }

    @Test
    fun `getBoolean with missing key and no default should return false`() {
        val key = "missing_bool_key"

        val retrieved = SecureStorage.getBoolean(context, key)

        assertFalse(retrieved)
    }

    @Test
    fun `storeInt should store and retrieve value correctly`() {
        val key = "test_int_key"
        val value = 42

        SecureStorage.storeInt(context, key, value)
        val retrieved = SecureStorage.getInt(context, key)

        assertEquals(value, retrieved)
    }

    @Test
    fun `storeInt should handle negative values`() {
        val key = "test_negative_key"
        val value = -999

        SecureStorage.storeInt(context, key, value)
        val retrieved = SecureStorage.getInt(context, key)

        assertEquals(value, retrieved)
    }

    @Test
    fun `storeInt should handle zero`() {
        val key = "test_zero_key"
        val value = 0

        SecureStorage.storeInt(context, key, value)
        val retrieved = SecureStorage.getInt(context, key)

        assertEquals(value, retrieved)
    }

    @Test
    fun `getInt with missing key should return default value`() {
        val key = "missing_int_key"
        val defaultValue = 100

        val retrieved = SecureStorage.getInt(context, key, defaultValue)

        assertEquals(defaultValue, retrieved)
    }

    @Test
    fun `getInt with missing key and no default should return zero`() {
        val key = "missing_int_key"

        val retrieved = SecureStorage.getInt(context, key)

        assertEquals(0, retrieved)
    }

    @Test
    fun `storeLong should store and retrieve value correctly`() {
        val key = "test_long_key"
        val value = 1234567890123L

        SecureStorage.storeLong(context, key, value)
        val retrieved = SecureStorage.getLong(context, key)

        assertEquals(value, retrieved)
    }

    @Test
    fun `storeLong should handle negative values`() {
        val key = "test_neg_long_key"
        val value = -9876543210987L

        SecureStorage.storeLong(context, key, value)
        val retrieved = SecureStorage.getLong(context, key)

        assertEquals(value, retrieved)
    }

    @Test
    fun `storeLong should handle zero`() {
        val key = "test_zero_long_key"
        val value = 0L

        SecureStorage.storeLong(context, key, value)
        val retrieved = SecureStorage.getLong(context, key)

        assertEquals(value, retrieved)
    }

    @Test
    fun `getLong with missing key should return default value`() {
        val key = "missing_long_key"
        val defaultValue = 999999L

        val retrieved = SecureStorage.getLong(context, key, defaultValue)

        assertEquals(defaultValue, retrieved)
    }

    @Test
    fun `getLong with missing key and no default should return zero`() {
        val key = "missing_long_key"

        val retrieved = SecureStorage.getLong(context, key)

        assertEquals(0L, retrieved)
    }

    @Test
    fun `remove should remove specific key`() {
        val key = "test_remove_key"
        SecureStorage.storeString(context, key, "value")

        SecureStorage.remove(context, key)
        val retrieved = SecureStorage.getString(context, key)

        assertNull(retrieved)
    }

    @Test
    fun `remove with non-existent key should not throw`() {
        val key = "non_existent_key"

        SecureStorage.remove(context, key)
    }

    @Test
    fun `clear should remove all keys`() {
        SecureStorage.storeString(context, "key1", "value1")
        SecureStorage.storeInt(context, "key2", 42)
        SecureStorage.storeBoolean(context, "key3", true)

        SecureStorage.clear(context)

        assertNull(SecureStorage.getString(context, "key1"))
        assertEquals(0, SecureStorage.getInt(context, "key2"))
        assertFalse(SecureStorage.getBoolean(context, "key3"))
    }

    @Test
    fun `clear should work on empty storage`() {
        SecureStorage.clear(context)
    }

    @Test
    fun `contains should return true for existing key`() {
        val key = "test_contains_key"
        SecureStorage.storeString(context, key, "value")

        val result = SecureStorage.contains(context, key)

        assertTrue(result)
    }

    @Test
    fun `contains should return false for non-existent key`() {
        val key = "non_existent_contains_key"

        val result = SecureStorage.contains(context, key)

        assertFalse(result)
    }

    @Test
    fun `contains should return false after remove`() {
        val key = "test_contains_remove_key"
        SecureStorage.storeString(context, key, "value")
        SecureStorage.remove(context, key)

        val result = SecureStorage.contains(context, key)

        assertFalse(result)
    }

    @Test
    fun `getAll should return all stored values`() {
        SecureStorage.storeString(context, "key1", "value1")
        SecureStorage.storeInt(context, "key2", 42)
        SecureStorage.storeBoolean(context, "key3", true)

        val all = SecureStorage.getAll(context)

        assertEquals(3, all.size)
        assertTrue(all.containsKey("key1"))
        assertTrue(all.containsKey("key2"))
        assertTrue(all.containsKey("key3"))
    }

    @Test
    fun `getAll should return empty map for empty storage`() {
        val all = SecureStorage.getAll(context)

        assertTrue(all.isEmpty())
    }

    @Test
    fun `getAll should return updated values after modifications`() {
        SecureStorage.storeString(context, "key", "initial")
        SecureStorage.storeString(context, "key", "updated")

        val all = SecureStorage.getAll(context)

        assertEquals("updated", all["key"])
    }

    @Test
    fun `should handle empty key string`() {
        val key = ""
        val value = "empty_key_value"

        SecureStorage.storeString(context, key, value)
        val retrieved = SecureStorage.getString(context, key)

        assertEquals(value, retrieved)
    }

    @Test
    fun `should handle empty string value`() {
        val key = "empty_value_key"
        val value = ""

        SecureStorage.storeString(context, key, value)
        val retrieved = SecureStorage.getString(context, key)

        assertEquals(value, retrieved)
    }

    @Test
    fun `should handle long string values`() {
        val key = "long_string_key"
        val value = "a".repeat(10000)

        SecureStorage.storeString(context, key, value)
        val retrieved = SecureStorage.getString(context, key)

        assertEquals(value, retrieved)
    }

    @Test
    fun `should handle special characters in keys`() {
        val key = "key-with_special.chars!@#$%"
        val value = "special_key_value"

        SecureStorage.storeString(context, key, value)
        val retrieved = SecureStorage.getString(context, key)

        assertEquals(value, retrieved)
    }

    @Test
    fun `should handle unicode characters in values`() {
        val key = "unicode_key"
        val value = "Hello 你好 🌍 🎉"

        SecureStorage.storeString(context, key, value)
        val retrieved = SecureStorage.getString(context, key)

        assertEquals(value, retrieved)
    }

    @Test
    fun `should handle maximum Int value`() {
        val key = "max_int_key"
        val value = Int.MAX_VALUE

        SecureStorage.storeInt(context, key, value)
        val retrieved = SecureStorage.getInt(context, key)

        assertEquals(value, retrieved)
    }

    @Test
    fun `should handle minimum Int value`() {
        val key = "min_int_key"
        val value = Int.MIN_VALUE

        SecureStorage.storeInt(context, key, value)
        val retrieved = SecureStorage.getInt(context, key)

        assertEquals(value, retrieved)
    }

    @Test
    fun `should handle maximum Long value`() {
        val key = "max_long_key"
        val value = Long.MAX_VALUE

        SecureStorage.storeLong(context, key, value)
        val retrieved = SecureStorage.getLong(context, key)

        assertEquals(value, retrieved)
    }

    @Test
    fun `should handle minimum Long value`() {
        val key = "min_long_key"
        val value = Long.MIN_VALUE

        SecureStorage.storeLong(context, key, value)
        val retrieved = SecureStorage.getLong(context, key)

        assertEquals(value, retrieved)
    }

    @Test
    fun `should overwrite existing values`() {
        val key = "overwrite_key"
        SecureStorage.storeString(context, key, "initial_value")
        SecureStorage.storeString(context, key, "updated_value")

        val retrieved = SecureStorage.getString(context, key)

        assertEquals("updated_value", retrieved)
    }
}
