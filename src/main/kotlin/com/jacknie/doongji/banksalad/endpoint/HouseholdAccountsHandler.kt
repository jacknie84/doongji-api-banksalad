package com.jacknie.doongji.banksalad.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.jacknie.doongji.banksalad.model.HouseholdAccounts
import com.jacknie.doongji.banksalad.model.HouseholdAccountsRepository
import com.jacknie.doongji.banksalad.model.ModelRepository
import com.jacknie.doongji.banksalad.model.Selector
import org.jooq.generated.public_.Tables
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.queryParamOrNull
import reactor.core.publisher.Mono

class HouseholdAccountsHandler(
        private val modelRepository: ModelRepository,
        private val householdAccountsRepository: HouseholdAccountsRepository,
        private val objectMapper: ObjectMapper) {

    fun getList(request: ServerRequest): Mono<out ServerResponse> {
        val q = request.queryParamOrNull("q")?: "{}"
        val selector = objectMapper.readValue<Selector>(q)
        val table = Tables.DOONGJI_HOUSEHOLD_ACCOUNTS
        return modelRepository.findAll(table, selector, HouseholdAccounts::class.java).flatMap { responseOf(it) }
    }

    fun get(request: ServerRequest): Mono<out ServerResponse> {
        val id = request.pathVariable("id").toLong()
        return householdAccountsRepository.findById(id)
                .flatMap { ServerResponse.ok().bodyValue(it) }
                .switchIfEmpty(ServerResponse.notFound().build())
    }
}