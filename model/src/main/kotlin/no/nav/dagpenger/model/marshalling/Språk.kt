package no.nav.dagpenger.model.marshalling

import no.nav.dagpenger.model.faktum.Faktum
import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle

class Språk(private val lokal: Locale = bokmål, versjonId: Int) {

    private val basename = "oversettelser_v$versjonId"

    companion object {
        private const val NO = "NO"
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
    fun oversett(faktum: Faktum<*>) = nøkkel(faktum).oversett(faktum.navn)

    fun nøkkel(faktum: Faktum<*>) =
        """faktum_${faktum.reflection { rootId, _ -> rootId }}_navn"""
}
