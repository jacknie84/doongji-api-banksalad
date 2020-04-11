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
        return findBySelector(selector).flatMap { responseOf(it) }
    }

    fun get(request: ServerRequest): Mono<out ServerResponse> {
        val id = request.pathVariable("id").toLong()
        return householdAccountsRepository.findById(id)
                .flatMap { ServerResponse.ok().bodyValue(it) }
                .switchIfEmpty(ServerResponse.notFound().build())
    }

    private fun findBySelector(selector: Selector): Mono<out Page<HouseholdAccounts>> {
        val pageable = toPageable(selector.pagination?: Pagination(1, 10, null))
        val criteria = toCriteria(selector.condition)
        return client.select()
                .from(HouseholdAccounts::class.java)
                .project("id")
                .matching(criteria)
                .fetch().all()
                .collect(Collectors.summingLong { 1 })
                .filter { it > 0 }
                .zipWith(client.select()
                        .from(HouseholdAccounts::class.java)
                        .project(*selector.selectedFields.map { it.toSnakeCase() }.toTypedArray())
                        .page(pageable)
                        .matching(criteria)
                        .fetch().all().collectList())
                .map { PageImpl(it.t2, pageable, it.t1) }
                .defaultIfEmpty(PageImpl(emptyList(), pageable, 0))
    }

    private fun toPageable(pagination: Pagination): Pageable {
        val (page, size, orders) = pagination
        val sort = Sort.by(orders?.map { Sort.Order(it.direction, it.property.toSnakeCase()) }?: emptyList())
        return PageRequest.of(page, size, sort)
    }

    private fun toCriteria(condition: Condition): Criteria {
        val predicates = condition.predicates.toList()
        val criteria = Criteria.where(predicates[0].field).accept(predicates[0])
        return predicates.subList(1, predicates.size).fold(criteria) { acc, p -> acc.accept(p) }
    }

    private fun Criteria.accept(predicate: Predicate): Criteria {
        val (field, operator, values) = predicate
        return if ((operator != Operator.IS_NULL || operator != Operator.IS_NOT_NULL) && values.isEmpty()) {
            this
        }
        else {
            and(field.toSnakeCase()).accept(predicate)
        }
    }

    private fun Criteria.CriteriaStep.accept(predicate: Predicate): Criteria {
        val (field, operator, values) = predicate
        return when (operator) {
            Operator.IS_NULL -> isNull
            Operator.IS_NOT_NULL -> isNotNull
            Operator.EQUALS -> when {
                values.size > 1 -> isIn(values)
                else -> isEquals(values[0])
            }
            Operator.NOT_EQUALS -> when {
                values.size > 1 -> notIn(values)
                else -> not(values[0])
            }
            Operator.IN -> isIn(values)
            Operator.NOT_IN -> notIn(values)
            Operator.GREATER_THAN -> values.subList(1, values.size)
                    .fold(greaterThan(values[0])) { criteria, s -> criteria.or(field.toSnakeCase()).greaterThan(s) }
            Operator.GREATER_THAN_EQUALS -> values.subList(1, values.size)
                    .fold(greaterThanOrEquals(values[0])) { criteria, s -> criteria.or(field.toSnakeCase()).greaterThanOrEquals(s) }
            Operator.LESS_THAN -> values.subList(1, values.size)
                    .fold(lessThan(values[0])) { criteria, s -> criteria.or(field.toSnakeCase()).lessThan(s) }
            Operator.LESS_THAN_EQUALS -> values.subList(1, values.size)
                    .fold(lessThanOrEquals(values[0])) { criteria, s -> criteria.or(field.toSnakeCase()).lessThanOrEquals(s) }
            Operator.STARTS_WITH -> values.subList(1, values.size)
                    .fold(like("${values[0]}%")) { criteria, s -> criteria.or(field.toSnakeCase()).like("$s%") }
            Operator.ENDS_WITH -> values.subList(1, values.size)
                    .fold(like("%${values[0]}")) { criteria, s -> criteria.or(field.toSnakeCase()).like("%$s") }
            Operator.CONTAINS ->values.subList(1, values.size)
                    .fold(like("%${values[0]}%")) { criteria, s -> criteria.or(field.toSnakeCase()).like("%$s%") }
        }
    }

    private fun String.toSnakeCase() = fold(StringBuilder(length)) { acc, c ->
        if (c in 'A'..'Z') {
            if (acc.isNotEmpty()) {
                acc.append("_")
            }
            acc.append(c + ('a' - 'A'))
        }
        else {
            acc.append(c)
        }
    }.toString()
}