package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object EmailVerificationService {

    private const val TAG = "EmailVerification"
    private const val RESEND_URL = "https://api.resend.com/emails"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Sends a 6-digit verification code to the specified email using the Resend API.
     * In Resend testing mode (without custom domain), emails are delivered to onboarding/verified emails
     * or via onboarding@resend.dev.
     */
    suspend fun sendVerificationCode(
        recipientEmail: String,
        code: String,
        cafeName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.RESEND_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_RESEND_API_KEY") {
            Log.e(TAG, "Resend API key is missing or not configured in BuildConfig!")
            return@withContext Result.failure(
                IllegalStateException("مفتاح API الخاص بإرسال البريد غير متوفر")
            )
        }

        try {
            val jsonPayload = JSONObject().apply {
                put("from", "كافيه النخيل <onboarding@resend.dev>")
                put("to", JSONArray().apply { put(recipientEmail) })
                put("subject", "رمز تأكيد حسابك في $cafeName هو: $code")
                put("html", """
                    <div dir="rtl" style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 500px; margin: auto; padding: 25px; border-radius: 16px; background-color: #1a1614; color: #ffffff; border: 1px solid #3d312a;">
                        <h2 style="color: #f59e0b; margin-bottom: 12px; text-align: center;">$cafeName</h2>
                        <h3 style="color: #e2e8f0; text-align: center; margin-top: 0;">رمز التحقق لتسجيل الحساب</h3>
                        <p style="color: #94a3b8; font-size: 15px; line-height: 1.6; text-align: center;">
                            أهلاً بك! استخدم رمز التأكيد التالي لإكمال إنشاء حسابك وتأكيد بريدك الإلكتروني:
                        </p>
                        <div style="background-color: #27201c; padding: 18px; border-radius: 12px; text-align: center; margin: 25px 0; border: 2px dashed #f59e0b;">
                            <span style="font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #f59e0b; font-family: monospace;">$code</span>
                        </div>
                        <p style="color: #64748b; font-size: 13px; text-align: center;">
                            هذا الرمز صالح لمدة 10 دقائق فقط. إذا لم تقم بطلب هذا الرمز، يمكنك تجاهل هذه الرسالة بأمان.
                        </p>
                        <hr style="border: none; border-top: 1px solid #33261f; margin: 20px 0;" />
                        <p style="color: #475569; font-size: 12px; text-align: center; margin-bottom: 0;">
                            نظام إدارة الكافيه والمينو الرقمي السحابي
                        </p>
                    </div>
                """.trimIndent())
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonPayload.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(RESEND_URL)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Log.d(TAG, "OTP email successfully sent to $recipientEmail: $responseBody")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Failed to send email. Code: ${response.code}, Response: $responseBody")
                // Parse error message if possible
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    errJson.optString("message", "فشل إرسال البريد الإلكتروني")
                } catch (_: Exception) {
                    "فشل إرسال البريد الإلكتروني (${response.code})"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception sending OTP email", e)
            Result.failure(e)
        }
    }
}
