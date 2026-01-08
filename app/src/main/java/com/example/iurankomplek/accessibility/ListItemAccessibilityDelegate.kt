package com.example.iurankomplek.accessibility

import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.AccessibilityDelegateCompat
import com.example.iurankomplek.R

class ListItemAccessibilityDelegate(
    private val name: String?,
    private val email: String?,
    private val address: String?,
    private val iuranPerwarga: String?,
    private val totalIuranIndividu: String?
) : AccessibilityDelegateCompat() {

    override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(host, info)
        
        val description = buildDescription()
        if (description.isNotEmpty()) {
            info.text = description
            info.contentDescription = description
        }
        
        info.className = "android.widget.ListView"
    }

    override fun onPopulateAccessibilityEvent(host: View, event: AccessibilityEvent) {
        super.onPopulateAccessibilityEvent(host, event)
        
        val description = buildDescription()
        if (description.isNotEmpty()) {
            event.text.add(description)
        }
    }

    private fun buildDescription(): String {
        return buildString {
            if (!name.isNullOrBlank()) {
                append("Name: $name")
            }
            if (!email.isNullOrBlank()) {
                if (isNotEmpty()) append(", ")
                append("Email: $email")
            }
            if (!address.isNullOrBlank()) {
                if (isNotEmpty()) append(", ")
                append("Address: $address")
            }
            if (!iuranPerwarga.isNullOrBlank()) {
                if (isNotEmpty()) append(", ")
                append("Monthly fee: $iuranPerwarga")
            }
            if (!totalIuranIndividu.isNullOrBlank()) {
                if (isNotEmpty()) append(", ")
                append("Total individual fee: $totalIuranIndividu")
            }
        }
    }
}
