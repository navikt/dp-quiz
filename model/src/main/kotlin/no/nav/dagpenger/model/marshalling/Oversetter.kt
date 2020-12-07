package no.nav.dagpenger.model.marshalling

import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle

private const val NO = "NO"
val bokmål = Locale("nb", NO)
val nynorsk = Locale("nn", NO)

fun String.oversett(lokal: Locale = bokmål): String =
    try {
        ResourceBundle.getBundle("oversettelser", lokal)
            .getString(this)
    } catch (exception: MissingResourceException) {
        this
    }
