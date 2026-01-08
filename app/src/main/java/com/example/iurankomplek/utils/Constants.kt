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
        
        // Connection Pool Constants
        const val MAX_IDLE_CONNECTIONS = 5
        const val KEEP_ALIVE_DURATION_MINUTES = 5L
        
        // Rate Limiting Constants
        const val MAX_REQUESTS_PER_SECOND = 10
        const val MAX_REQUESTS_PER_MINUTE = 60
    }
    
    // API Constants
    object Api {
        const val PRODUCTION_BASE_URL = "https://api.apispreadsheets.com/data/"
        const val MOCK_BASE_URL = "https://api-mock:5000/data/"
        const val DOCKER_ENV_KEY = "DOCKER_ENV"
        
        // API Versioning
        const val API_VERSION = "v1"
        const val API_VERSION_PREFIX = "api/$API_VERSION/"
        
        // Version Strategy: Path-based versioning (e.g., /api/v1/users)
        // Backward compatibility: Maintain non-versioned endpoints until deprecation
        // Deprecation timeline: 6 months notice before removing old endpoints
    }
    
    // Security Constants
    object Security {
        const val CERTIFICATE_PINNER = "sha256/PIdO5FV9mQyEclv5rMC4oGNTya7Q9S5/Sn1KTWpQov0=" +
                ";sha256/G9LNNAql897egYsabashkzUCTEJkWBzgoEtk8X/678c=" +
                ";sha256/++MBgDH5WGvL9Bcn5Be30cRcL0f5O+NyoXuWtQdX1aI="
        // Certificate pins extracted on 2026-01-08
        // Primary: PIdO5FV9mQyEclv5rMC4oGNTya7Q9S5/Sn1KTWpQov0=
        // Backup #1: G9LNNAql897egYsabashkzUCTEJkWBzgoEtk8X/678c=
        // Backup #2: ++MBgDH5WGvL9Bcn5Be30cRcL0f5O+NyoXuWtQdX1aI=
        // Reference: https://developer.android.com/training/articles/security-ssl#Pinning
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
        const val ERROR_HANDLER = "ErrorHandler"
        const val API_CLIENT = "ApiClient"
        const val CIRCUIT_BREAKER = "CircuitBreaker"
        const val RATE_LIMITER = "RateLimiter"
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
        const val MAX_PAYMENT_AMOUNT = 999999999.99
    }

    // Webhook Constants
    object Webhook {
        const val MAX_RETRIES = 5
        const val INITIAL_RETRY_DELAY_MS = 1000L
        const val MAX_RETRY_DELAY_MS = 60000L
        const val RETRY_BACKOFF_MULTIPLIER = 2.0
        const val IDEMPOTENCY_KEY_PREFIX = "whk_"
        const val MAX_EVENT_RETENTION_DAYS = 30
        const val RETRY_JITTER_MS = 500L
        const val DEFAULT_RETRY_LIMIT = 50
    }

    // Circuit Breaker Constants
    object CircuitBreaker {
        const val DEFAULT_TIMEOUT_MS = 60000L
        const val DEFAULT_FAILURE_THRESHOLD = 5
        const val DEFAULT_SUCCESS_THRESHOLD = 2
        const val DEFAULT_HALF_OPEN_MAX_CALLS = 3
    }

    // Image Constants
    object Image {
        const val LOAD_TIMEOUT_MS = 10000L
    }
}
