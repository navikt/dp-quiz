package no.nav.dagpenger.quiz.mediator.helpers

import no.nav.dagpenger.model.seksjon.Versjon

fun versjonId() = versjonId++
private var versjonId = runCatching { Versjon.siste }.getOrDefault(1000)
