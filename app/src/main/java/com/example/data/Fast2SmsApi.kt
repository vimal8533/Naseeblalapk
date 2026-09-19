package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * Fast2SMS API response model.
 */
data class Fast2SmsResponse(
    @param:Json(name = "return") val returnStatus: Boolean = false,
    @param:Json(name = "request_id") val requestId: String? = null,
    @param:Json(name = "message") val message: List<String>? = null,
    @param:Json(name = "status_code") val statusCode: Int? = null
)

/**
 * Retrofit interface for Fast2SMS Cloud API.
 * Adheres to Fast2SMS Quick SMS specifications:
 * - URL: https://www.fast2sms.com/dev/bulkV2
 * - Method: POST (application/x-www-form-urlencoded)
 * - Header: authorization: <API_KEY>
 */
interface Fast2SmsApi {

    /**
     * Sends Quick SMS via Fast2SMS Cloud Gateway.
     *
     * @param apiKey Authorization API Key header
     * @param route Route identifier ("q" for Quick SMS)
     * @param message Text message body
     * @param language Message language ("english" or "unicode")
     * @param flash Flash SMS flag (0 for regular, 1 for flash)
     * @param numbers 10-digit mobile number(s) comma separated
     */
    @FormUrlEncoded
    @POST("dev/bulkV2")
    @Headers(
        "Cache-Control: no-cache",
        "Content-Type: application/x-www-form-urlencoded"
    )
    suspend fun sendQuickSms(
        @Header("authorization") apiKey: String,
        @Field("route") route: String = "q",
        @Field("message") message: String,
        @Field("language") language: String = "english",
        @Field("flash") flash: Int = 0,
        @Field("numbers") numbers: String
    ): Response<Fast2SmsResponse>

    companion object {
        const val BASE_URL = "https://www.fast2sms.com/"

        val instance: Fast2SmsApi by lazy {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(Fast2SmsApi::class.java)
        }
    }
}
