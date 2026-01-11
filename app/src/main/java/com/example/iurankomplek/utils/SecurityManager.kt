package com.example.iurankomplek.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.iurankomplek.BuildConfig
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * SecurityManager handles various security-related tasks including
 * certificate monitoring, security checks, and security configuration management.
 */
object SecurityManager {
    private val TAG = Constants.Tags.SECURITY_MANAGER

    private val SU_PATHS = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su"
    )

    private val DANGEROUS_APPS = arrayOf(
        "com.noshufou.android.su",
        "com.thirdparty.superuser",
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser",
        "com.topjohnwu.magisk",
        "com.devadvance.rootcloak2",
        "com.saurik.substrate",
        "com.zachspong.temprootremovejb",
        "com.amphoras.hidemyroot",
        "com.formyhm.hideroot",
        "com.formyhm.hiderootPremium",
        "de.robv.android.xposed.installer",
        "com.saurik.substrate",
        "com.zacharee1.superuser"
    )

    private val EMULATOR_INDICATORS = arrayOf(
        "goldfish",
        "ranchu",
        "generic",
        "sdk_gphone",
        "unknown"
    )

    private val EMULATOR_MODELS = arrayOf(
        "Emulator",
        "Android SDK built for x86",
        "Android SDK built for x86_64",
        "Google SDK",
        "Vbox86p",
        "vmware",
        "VirtualBox"
    )

    private val EMULATOR_HARDWARE = arrayOf(
        "goldfish",
        "ranchu",
        "vbox",
        "qemu",
        "generic"
    )

    private val EMULATOR_HOSTS = arrayOf(
        "10.0.2.2",
        "10.0.2.15",
        "10.0.2.3"
    )

    private val EMULATOR_PROPERTIES = arrayOf(
        "ro.product.model",
        "ro.hardware",
        "ro.kernel.qemu",
        "ro.bootloader"
    )

    /**
     * Checks if the app is running in a secure environment
     * @return true if the environment is secure (not rooted, not emulated)
     */
    fun isSecureEnvironment(context: Context): Boolean {
        return !isDeviceRooted(context) && !isDeviceEmulator(context)
    }

    /**
     * Checks if the device is rooted
     * @return true if the device is rooted, false otherwise
     */
    fun isDeviceRooted(context: Context): Boolean {
        return checkSuBinary() || checkDangerousApps(context) || checkRootManagementApps(context) || checkSystemProps()
    }

    /**
     * Checks for the presence of su binary
     */
    private fun checkSuBinary(): Boolean {
        for (path in SU_PATHS) {
            if (File(path).exists()) {
                return true
            }
        }
        return false
    }

    /**
     * Checks for known dangerous/ rooting apps
     */
    private fun checkDangerousApps(context: Context): Boolean {
        return DANGEROUS_APPS.any { app ->
            try {
                val pm = context.packageManager
                pm.getPackageInfo(app, 0)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Checks for root management apps (SuperSU, Magisk, etc.)
     */
    private fun checkRootManagementApps(context: Context): Boolean {
        val rootApps = listOf(
            "com.noshufou.android.su",
            "com.topjohnwu.magisk",
            "eu.chainfire.supersu"
        )
        return rootApps.any { app ->
            try {
                val pm = context.packageManager
                pm.getPackageInfo(app, 0)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Checks for root-related system properties
     */
    private fun checkSystemProps(): Boolean {
        val props = arrayOf(
            "service.adb.root",
            "ro.secure",
            "ro.debuggable"
        )
        return props.any { prop ->
            try {
                val value = getSystemProperty(prop)
                when (prop) {
                    "service.adb.root" -> value == "1"
                    "ro.secure" -> value == "0"
                    "ro.debuggable" -> value == "1"
                    else -> false
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Gets a system property value
     */
    @SuppressLint("PrivateApi")
    private fun getSystemProperty(prop: String): String? {
        return try {
            val clz = Class.forName("android.os.SystemProperties")
            val get = clz.getMethod("get", String::class.java)
            get.invoke(clz, prop) as? String
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Checks if the device is an emulator
     * @return true if the device is an emulator, false otherwise
     */
    @SuppressLint("HardwareIds")
    fun isDeviceEmulator(context: Context): Boolean {
        return checkBuildManufacturer() ||
                checkBuildModel() ||
                checkBuildProduct() ||
                checkBuildHardware() ||
                checkTelephony(context) ||
                checkEmulatorFiles() ||
                checkEmulatorHosts()
    }

    /**
     * Checks build manufacturer for emulator indicators
     */
    private fun checkBuildManufacturer(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return manufacturer.contains("generic") ||
                manufacturer.contains("unknown") ||
                manufacturer.contains("genymotion") ||
                manufacturer.contains("emulator") ||
                manufacturer.contains("android sdk")
    }

    /**
     * Checks build model for emulator indicators
     */
    private fun checkBuildModel(): Boolean {
        val model = Build.MODEL.lowercase()
        return EMULATOR_MODELS.any { model.contains(it.lowercase()) }
    }

    /**
     * Checks build product for emulator indicators
     */
    private fun checkBuildProduct(): Boolean {
        val product = Build.PRODUCT.lowercase()
        return product.contains("sdk") ||
                product.contains("google_sdk") ||
                product.contains("sdk_gphone") ||
                product.contains("vbox") ||
                product.contains("emulator")
    }

    /**
     * Checks build hardware for emulator indicators
     */
    private fun checkBuildHardware(): Boolean {
        val hardware = Build.HARDWARE.lowercase()
        return EMULATOR_HARDWARE.any { hardware.contains(it.lowercase()) }
    }

    /**
     * Checks telephony for emulator indicators
     */
    private fun checkTelephony(context: Context): Boolean {
        return try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? android.telephony.TelephonyManager
            val deviceId = tm?.deviceId
            deviceId == null || deviceId == "000000000000000"
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks for emulator-specific files
     */
    private fun checkEmulatorFiles(): Boolean {
        val files = arrayOf(
            "/dev/socket/qemud",
            "/dev/qemu_pipe",
            "/system/bin/qemu-props"
        )
        return files.any { File(it).exists() }
    }

    /**
     * Checks for emulator-specific DNS hosts
     */
    private fun checkEmulatorHosts(): Boolean {
        return try {
            val hosts = File("/etc/hosts")
            if (hosts.exists()) {
                BufferedReader(InputStreamReader(hosts.inputStream())).use { reader ->
                    reader.readLines().any { line ->
                        EMULATOR_HOSTS.any { host -> line.contains(host) }
                    }
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if the device is likely a real device (not emulator or rooted)
     * This is a more lenient check for non-critical functionality
     * @return true if the device appears to be a real device
     */
    fun isLikelyRealDevice(context: Context): Boolean {
        return !isDeviceEmulator(context) && !isDeviceRooted(context)
    }

    /**
     * Monitors certificate expiration and logs warnings
     */
    fun monitorCertificateExpiration() {
        val pins = BuildConfig.CERTIFICATE_PINNER.split(";")
        if (pins.isEmpty() || pins.all { it.isEmpty() }) {
            Log.w(TAG, "Certificate pinning is not configured")
            return
        }
        
        val currentDate = java.util.Date()
        val expirationDate = java.util.Date(176, 11, 31)
        val daysUntilExpiration = ((expirationDate.time - currentDate.time) / (1000 * 60 * 60 * 24)).toInt()
        
        if (daysUntilExpiration <= 90) {
            Log.w(TAG, "Certificate pinning expires soon. Consider rotating pins before expiration.")
        }
    }

    /**
     * Validates that security configurations are properly set up
     */
    fun validateSecurityConfiguration(): Boolean {
        var isValid = true
        
        if (BuildConfig.CERTIFICATE_PINNER.isBlank()) {
            Log.e(TAG, "Certificate pinning is not configured")
            isValid = false
        }
        
        val pins = BuildConfig.CERTIFICATE_PINNER.split(";")
        if (pins.size < 2) {
            Log.w(TAG, "Certificate pinning should have at least 2 pins for redundancy")
            isValid = false
        }
        
        if (isValid) {
            Log.d(TAG, "Security configuration validation passed")
        }
        
        return isValid
    }

    /**
     * Checks for potential security threats
     */
    fun checkSecurityThreats(context: Context): List<String> {
        val threats = mutableListOf<String>()

        if (!isSecureEnvironment(context)) {
            if (isDeviceRooted(context)) {
                threats.add("Device is rooted")
            }
            if (isDeviceEmulator(context)) {
                threats.add("Device appears to be an emulator")
            }
        }

        if (!validateSecurityConfiguration()) {
            threats.add("Security configuration is invalid")
        }

        return threats
    }

    data class SecurityReport(
        val isSecure: Boolean,
        val isRooted: Boolean,
        val isEmulator: Boolean,
        val certificatePinningValid: Boolean,
        val threats: List<String>
    )
}