package com.litego.browser
object AdBlocker {
    private val adHosts = setOf("doubleclick.net","googlesyndication.com","googleadservices.com","googletagmanager.com","adservice.google","adsystem.amazon","facebook.net/tr","facebook.com/tr","ads-twitter.com","analytics.twitter.com","hotjar.com","taboola.com","outbrain.com","popads.net","popcash.net")
    fun isAd(url: String): Boolean { val u = url.lowercase(); return adHosts.any { u.contains(it) } || u.contains("/ads/") || u.contains("adserver") }
}
