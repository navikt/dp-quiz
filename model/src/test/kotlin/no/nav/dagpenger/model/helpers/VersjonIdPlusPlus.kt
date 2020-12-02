package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.seksjon.Versjon

fun versjonId() = versjonId++
private var versjonId = runCatching { Versjon.siste }.getOrDefault(0)
