/**
 * It is part of Nexus. Nexus is a command helper for Minecraft Bedrock Edition.
 * Copyright (C) 2026  Akanyi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.nexuscmd.network

import android.content.Context
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.brotli.BrotliInterceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.nexuscmd.BuildConfig
import com.nexuscmd.network.chelper.service.NexusService
import com.nexuscmd.network.library.interceptor.AuthInterceptor
import com.nexuscmd.network.library.interceptor.RateLimitInterceptor
import com.nexuscmd.network.library.interceptor.WafInterceptor
import com.nexuscmd.network.library.service.CaptchaService
import com.nexuscmd.network.library.service.CommandLabPublicService
import com.nexuscmd.network.library.service.CommandLabUserService
import com.nexuscmd.network.library.util.WafHelper
import java.io.File
import java.util.concurrent.TimeUnit

object ServiceManager {
    lateinit var CLIENT: OkHttpClient
    lateinit var CHELPER_RETROFIT: Retrofit
    lateinit var COMMAND_LAB_RETROFIT: Retrofit
    lateinit var CHELPER_SERVICE: NexusService

    lateinit var COMMAND_LAB_PUBLIC_SERVICE: CommandLabPublicService
    lateinit var COMMAND_LAB_USER_SERVICE: CommandLabUserService

    lateinit var CAPTCHA_SERVICE: CaptchaService

    var LAB_BASE_URL = "https://abyssous.site/"

    fun init(context: Context) {
        val json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .cache(Cache(File(context.cacheDir, "http_cache"), 10 * 1024 * 1024))
            .addInterceptor(BrotliInterceptor)
            .addInterceptor(RateLimitInterceptor(2))
            .addInterceptor(WafInterceptor())
            .addInterceptor(AuthInterceptor.INSTANCE)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }
        CLIENT = builder.build()
        CHELPER_RETROFIT = Retrofit.Builder()
            .baseUrl("https://www.yanceymc.cn/api/chelper/")
            .client(CLIENT)
            .addConverterFactory(
                json.asConverterFactory(
                    "application/json; charset=utf-8".toMediaType()
                )
            )
            .build()
        COMMAND_LAB_RETROFIT = Retrofit.Builder()
            .baseUrl(LAB_BASE_URL)
            .client(CLIENT)
            .addConverterFactory(
                json.asConverterFactory(
                    "application/json; charset=utf-8".toMediaType()
                )
            )
            .build()
        CHELPER_SERVICE = CHELPER_RETROFIT.create(NexusService::class.java)
        COMMAND_LAB_PUBLIC_SERVICE =
            COMMAND_LAB_RETROFIT.create(CommandLabPublicService::class.java)
        COMMAND_LAB_USER_SERVICE = COMMAND_LAB_RETROFIT.create(CommandLabUserService::class.java)
        CAPTCHA_SERVICE = COMMAND_LAB_RETROFIT.create(CaptchaService::class.java)

        // 初始化 WAF Helper
        WafHelper.init(context)
    }
}
