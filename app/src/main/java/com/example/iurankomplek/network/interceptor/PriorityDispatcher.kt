package com.example.iurankomplek.network.interceptor

import okhttp3.Dispatcher

class PriorityDispatcher(
    private val maxRequestsPerHost: Int = 5,
    private val maxRequests: Int = 64
) {
    private val dispatcher = Dispatcher().apply {
        this.maxRequests = maxRequests
        this.maxRequestsPerHost = maxRequestsPerHost
    }

    fun getDispatcher(): Dispatcher {
        return dispatcher
    }

    fun getMaxRequestsPerHost(): Int {
        return maxRequestsPerHost
    }

    fun getMaxRequests(): Int {
        return maxRequests
    }

    fun cancelAll() {
        dispatcher.cancelAll()
    }
}
