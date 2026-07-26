package com.litego.browser
object AdBlocker {
    private val blockedHosts = setOf("doubleclick.net","googlesyndication.com","googleadservices.com","adservice.google","facebook.com/tr","ads-twitter.com")
    fun isAd(url: String): Boolean { return blockedHosts.any { url.contains(it) } }
}
