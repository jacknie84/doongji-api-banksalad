package com.jacknie.doongji.banksalad.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.jacknie.doongji.banksalad.model.*
import org.springframework.data.domain.*
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.isEquals
import org.springframework.data.r2dbc.core.isIn
import org.springframework.data.r2dbc.query.Criteria
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.queryParamOrNull
import reactor.core.publisher.Mono
import java.util.stream.Collectors

class HouseholdAccountsHandler(
        private val client: DatabaseClient,
        private val householdAccountsRepository: HouseholdAccountsRepository,
        private val objectMapper: ObjectMapper) {

    fun getList(request: ServerRequest): Mono<out ServerResponse> {
        val q = request.queryParamOrNull("q")?: "{}"
        val selector = objectMapper.readValue<Selector>(q)
        return findBySelector(client, selector, HouseholdAccounts::class.java)
                .flatMap { responseOf(it) }
    }

    fun get(request: ServerRequest): Mono<out ServerResponse> {
        val id = request.pathVariable("id").toLong()
        return householdAccountsRepository.findById(id)
                .flatMap { ServerResponse.ok().bodyValue(it) }
                .switchIfEmpty(ServerResponse.notFound().build())
    }

}