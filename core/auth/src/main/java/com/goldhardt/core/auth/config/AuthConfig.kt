package com.goldhardt.core.auth.config

import android.content.Context
import com.goldhardt.core.auth.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthConfig @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    
    fun getWebClientId(): String {
        return try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to get web client ID. Make sure default_web_client_id is configured in strings.xml", e)
        }
    }
}
