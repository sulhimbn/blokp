package com.example.iurankomplek.network.interceptor

enum class RequestPriority(val priorityLevel: Int) {
    CRITICAL(1),
    HIGH(2),
    NORMAL(3),
    LOW(4),
    BACKGROUND(5)
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Priority(val value: RequestPriority)
