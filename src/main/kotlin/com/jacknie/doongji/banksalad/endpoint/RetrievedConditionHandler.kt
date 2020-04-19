package com.jacknie.doongji.banksalad.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.jacknie.doongji.banksalad.model.*
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.isEquals
import org.springframework.data.r2dbc.query.Criteria
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono
import java.time.Instant

class RetrievedConditionHandler(
        private val client: DatabaseClient,
        private val retrievedConditionRepository: RetrievedConditionRepository,
        private val retrievedConditionPredicateRepository: RetrievedConditionPredicateRepository,
        private val objectMapper: ObjectMapper) {

    fun post(request: ServerRequest): Mono<out ServerResponse> {
        return request.bodyToMono<RetrievedCondition>()
                .doOnNext { it.lastRetrievedDate = Instant.now().toString() }
                .flatMap { retrievedConditionRepository.save(it) }
                .map { request.uriBuilder().path("/{id}").build(it.id) }
                .flatMap { ServerResponse.created(it).build() }
    }

    fun getList(request: ServerRequest): Mono<out ServerResponse> {
        val q = request.queryParamOrNull("q") ?: "{}"
        val selector = objectMapper.readValue<Selector>(q)
        return findBySelector(client, selector, RetrievedCondition::class.java)
                .flatMap { responseOf(it) }
    }

    fun get(request: ServerRequest): Mono<out ServerResponse> {
        val id = request.pathVariable("id").toLong()
        return retrievedConditionRepository.findById(id)
                .flatMap { ServerResponse.ok().bodyValue(it) }
                .switchIfEmpty(ServerResponse.notFound().build())
    }

    fun put(request: ServerRequest): Mono<out ServerResponse> {
        val id = request.pathVariable("id").toLong()
        return retrievedConditionRepository.findById(id)
            .zipWith(request.bodyToMono<RetrievedCondition>())
            .map {
                it.t1.lastRetrievedDate = Instant.now().toString()
                it.t1.favorite = it.t2.favorite
                it.t1.name = it.t2.name
                it.t1
            }
            .flatMap { retrievedConditionRepository.save(it) }
            .flatMap { ServerResponse.noContent().build() }
    }

    fun putPredicates(request: ServerRequest): Mono<out ServerResponse> {
        val conditionId = request.pathVariable("conditionId").toLong()
        return retrievedConditionRepository.existsById(conditionId)
                .flatMap { continuousMono(it) { request.bodyToFlux<RetrievedConditionPredicate>().collectList() } }
                .flatMap {
                    it.forEach { predicate -> predicate.conditionId = conditionId }
                    retrievedConditionPredicateRepository.saveAll(it).collectList()
                }
                .flatMap { client.delete()
                        .from(RetrievedConditionPredicate::class.java)
                        .matching(Criteria
                                .where("condition_id").isEquals(conditionId)
                                .and("id").notIn(it.map { predicate -> predicate.id }))
                        .fetch()
                        .rowsUpdated() }
                .flatMap { ServerResponse.noContent().build() }
                .switchIfEmpty(ServerResponse.notFound().build())
    }

    fun getPredicates(request: ServerRequest): Mono<out ServerResponse> {
        val conditionId = request.pathVariable("conditionId").toLong()
        return retrievedConditionPredicateRepository.findByConditionId(conditionId)
            .collectList()
            .flatMap { ServerResponse.ok().bodyValue(it) }
    }
}