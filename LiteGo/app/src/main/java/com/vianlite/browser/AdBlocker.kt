
package com.vianlite.browser

object AdBlocker {
    // Liste ultra légère - ajoute ici les domaines de pub
    private val blockedHosts = setOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "adservice.google",
        "facebook.com/tr",
        "ads-twitter.com"
    )
    fun isAd(url: String): Boolean {
        return blockedHosts.any { url.contains(it) }
    }
}
