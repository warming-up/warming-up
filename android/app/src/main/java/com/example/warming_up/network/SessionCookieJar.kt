package com.example.warming_up.network

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class SessionCookieJar(context: Context) : CookieJar {
    private val preferences = context.getSharedPreferences("auth_session", Context.MODE_PRIVATE)

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val sessionCookie = cookies.firstOrNull { it.name == SessionCookieName } ?: return
        preferences.edit()
            .putString(SessionCookieName, sessionCookie.value)
            .putString(SessionCookieDomainKey, sessionCookie.domain)
            .apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val sessionId = preferences.getString(SessionCookieName, null) ?: return emptyList()
        val domain = preferences.getString(SessionCookieDomainKey, url.host) ?: url.host

        return listOf(
            Cookie.Builder()
                .name(SessionCookieName)
                .value(sessionId)
                .domain(domain)
                .path("/")
                .build(),
        )
    }

    private companion object {
        const val SessionCookieName = "JSESSIONID"
        const val SessionCookieDomainKey = "JSESSIONID_DOMAIN"
    }
}
