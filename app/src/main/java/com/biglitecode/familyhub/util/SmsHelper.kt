package com.biglitecode.familyhub.util

import android.content.Context
import android.telephony.SmsManager
import android.widget.Toast

object SmsHelper {
    fun sendTaskReminder(
        context: Context,
        phoneNumber: String,
        taskTitle: String,
        assigneeName: String
    ): Boolean {
        if (phoneNumber.isBlank()) {
            Toast.makeText(context, "No phone number for $assigneeName", Toast.LENGTH_SHORT).show()
            return false
        }
        return try {
            val message = "FamilyHub reminder: Please complete \"$taskTitle\". — FamilyHub"
            @Suppress("DEPRECATION")
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(context, "Reminder SMS sent to $assigneeName", Toast.LENGTH_SHORT).show()
            true
        } catch (e: SecurityException) {
            Toast.makeText(context, "SMS permission denied", Toast.LENGTH_SHORT).show()
            false
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
