package com.example.iurankomplek.data.constraints

object ValidationRules {
    const val EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    
    object Numeric {
        const val MIN_VALUE = 0
        const val MAX_VALUE = 999999999
    }
    
    object Text {
        const val MIN_LENGTH = 1
    }
}
