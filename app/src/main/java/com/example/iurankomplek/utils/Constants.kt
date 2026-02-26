package com.example.iurankomplek.utils

/**
 * Centralized constants for the application
 */
object Constants {
    
    // Network Constants
    object Network {
        const val CONNECT_TIMEOUT = 30L
        const val READ_TIMEOUT = 30L
        const val WRITE_TIMEOUT = 30L
        const val MAX_RETRIES = 3
        const val INITIAL_RETRY_DELAY_MS = 1000L
        const val MAX_RETRY_DELAY_MS = 30000L
    }
    
    // Security Constants
    object Security {
        // Primary certificate pin for api.apispreadsheets.com
        // Expiration: 2028-12-31
        const val CERTIFICATE_PINNER = "sha256/PIdO5FV9mQyEclv5rMC4oGNTya7Q9S5/Sn1KTWpQov0="
        
        // Backup certificate pin for certificate rotation
        // SECURITY WARNING: This is a PLACEHOLDER - same as primary pin provides NO redundancy
        // MUST obtain actual backup pin from API provider before production deployment
        // Current implementation will fail if primary certificate rotates
        // To generate: openssl s_client -servername api.apispreadsheets.com -connect api.apispreadsheets.com:443 2>/dev/null | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64
        // TODO: Replace with actual backup pin from certificate provider
        const val BACKUP_CERTIFICATE_PINNER = "sha256/PLACEHOLDER_BACKUP_PIN_REQUIRED"
        
        // Array of all certificate pins for redundancy
        val ALL_CERTIFICATE_PINS = arrayOf(CERTIFICATE_PINNER, BACKUP_CERTIFICATE_PINNER)

        // Webhook Security Constants
        // PRODUCTION: MUST set BuildConfig.WEBHOOK_SECRET via CI/CD secrets
        // DEBUG: Falls back to this placeholder (should never be used in production)
        const val WEBHOOK_SECRET_KEY = "whsec_placeholder_replace_in_production"
        const val WEBHOOK_SIGNATURE_HEADER = "X-Webhook-Signature"
        const val WEBHOOK_TIMESTAMP_HEADER = "X-Webhook-Timestamp"
        const val WEBHOOK_TIMESTAMP_TOLERANCE_MS = 5 * 60 * 1000L // 5 minutes
    }
    
    // Financial Constants
    object Financial {
        const val IURAN_MULTIPLIER = 3
    }
    
    // Validation Constants
    object Validation {
        const val MAX_NAME_LENGTH = 50
        const val MAX_EMAIL_LENGTH = 100
        const val MAX_ADDRESS_LENGTH = 200
        const val MAX_PEMANFAATAN_LENGTH = 100
    }
    
    // Logging Tags
    object Tags {
        const val WEBHOOK_RECEIVER = "WebhookReceiver"
        const val SECURITY_MANAGER = "SecurityManager"
        const val BASE_ACTIVITY = "BaseActivity"
        const val USER_VIEW_MODEL = "UserViewModel"
        const val FINANCIAL_VIEW_MODEL = "FinancialViewModel"
        const val MAIN_ACTIVITY = "MainActivity"
        const val LAPORAN_ACTIVITY = "LaporanActivity"
    }
    
    // Toast Duration Constants
    object Toast {
        const val DURATION_SHORT = android.widget.Toast.LENGTH_SHORT
        const val DURATION_LONG = android.widget.Toast.LENGTH_LONG
    }
    
    // Payment Constants
    object Payment {
        const val DEFAULT_REFUND_AMOUNT_MIN = 1000
        const val REFUND_AMOUNT_RANGE_MIN = 1000
        const val REFUND_AMOUNT_RANGE_MAX = 9999
    }
}
