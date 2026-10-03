package com.example.services

import android.content.Context
import android.util.Log
import com.example.model.PaymentRecord
import com.example.utils.SecureConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * SQL Service for synchronizing with https://vendoros.ng/api backend
 * Manages offline queues via SharedPreferences when device is offline.
 */
object SqlService {

    private const val TAG = "SqlService"
    private const val PREFS_NAME = "vendoros_sql_prefs"
    private const val KEY_SQL_QUEUE = "sql_queue"

    private val httpClient = OkHttpClient.Builder().build()

    fun queueOfflineSync(context: Context, payment: PaymentRecord) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val currentQueueStr = prefs.getString(KEY_SQL_QUEUE, "[]") ?: "[]"
            val array = JSONArray(currentQueueStr)
            val obj = JSONObject().apply {
                put("paymentId", payment.paymentId)
                put("uid", payment.uid)
                put("email", payment.email)
                put("plan", payment.plan)
                put("amount", payment.amount)
                put("reference", payment.reference)
                put("secureLink", payment.secureLink)
                put("timestamp", System.currentTimeMillis())
            }
            array.put(obj)
            prefs.edit().putString(KEY_SQL_QUEUE, array.toString()).apply()
            Log.d(TAG, "Payment queued locally for SQL background sync: ${payment.reference}")
        } catch (e: Exception) {
            Log.e(TAG, "Error queueing offline SQL sync", e)
        }
    }

    suspend fun processOfflineQueue(context: Context): Int = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentQueueStr = prefs.getString(KEY_SQL_QUEUE, "[]") ?: "[]"
        val array = JSONArray(currentQueueStr)
        if (array.length() == 0) return@withContext 0

        var syncedCount = 0
        val remaining = JSONArray()

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val ref = item.optString("reference")
            val secToken = SecureConfig.sha256(ref)
            try {
                val request = Request.Builder()
                    .url("${SecureConfig.apiBase}/sync.php")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Secure-Token", secToken)
                    .post(item.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    syncedCount++
                } else {
                    remaining.put(item)
                }
            } catch (e: Exception) {
                remaining.put(item)
            }
        }

        prefs.edit().putString(KEY_SQL_QUEUE, remaining.toString()).apply()
        return@withContext syncedCount
    }
}
