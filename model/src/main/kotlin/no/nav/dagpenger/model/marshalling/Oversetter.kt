package no.nav.dagpenger.model.marshalling

import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle

class Oversetter {

    companion object {
        private const val NO = "NO"
        val bokmål = Locale("nb", NO)
        val nynorsk = Locale("nn", NO)
    }
}

fun String.oversett(lokal: Locale): String =
    this
        .toLowerCase()
        .replace(" ", "_").also {
            return try {
                ResourceBundle.getBundle("oversettelser", lokal)
                    .getString(it)
            } catch (exception: MissingResourceException) {
                this
            }
        }
