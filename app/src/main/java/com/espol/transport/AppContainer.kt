package com.espol.transport

import android.content.Context
import com.espol.transport.data.api.ApiProvider
import com.espol.transport.data.api.ApiService
import com.espol.transport.data.local.TokenStore

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val tokenStore = TokenStore(appContext)
    val api: ApiService by lazy { ApiProvider.create(tokenStore) }
}
