package no.nav.dagpenger.model.marshalling

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.regel.Regel
import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle

internal class Oversetter(private val lokal: Locale = bokmål, private val versjonId: Int, private val basename: String = defaultBaseName) {
    companion object {
        private const val NO = "NO"
        private const val defaultBaseName = "oversettelser"
        val bokmål = Locale("nb", NO)
        val nynorsk = Locale("nn", NO)
    }

    private fun String.oversett(defaultVerdi: String): String =
        try {
            ResourceBundle.getBundle(basename, lokal)
                .getString(this)
        } catch (exception: MissingResourceException) {
            defaultVerdi
        }
    fun oversett(faktum: Faktum<*>, felt: String) =
        // TODO ta ut verdi fra riktig felt
        """v_${versjonId}_faktum_${faktum.id}_$felt""".oversett(faktum.navn)

    fun oversett(regel: Regel, felt: String) =
        // TODO ta ut verdi fra riktig felt
        """v_${versjonId}_regel_${regel.typeNavn}_$felt""".oversett(regel.typeNavn)
}
