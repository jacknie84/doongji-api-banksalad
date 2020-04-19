package com.jacknie.doongji.banksalad.endpoint

import com.jacknie.doongji.banksalad.model.*
import org.springframework.data.domain.*
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.isEquals
import org.springframework.data.r2dbc.core.isIn
import org.springframework.data.r2dbc.query.Criteria
import reactor.core.publisher.Mono
import java.util.stream.Collectors

fun <T> findBySelector(client: DatabaseClient, selector: Selector, table: Class<T>): Mono<out Page<T>> {
    val pageable = toPageable(selector.pagination?: Pagination(1, 10, null))
    val criteria = toCriteria(selector.condition)
    return client.select()
            .from(table)
            .project("id")
            .matching(criteria)
            .fetch().all()
            .collect(Collectors.summingLong { 1 })
            .filter { it > 0 }
            .zipWith(client.select()
                    .from(table)
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
    return PageRequest.of(if (page > 0) page - 1 else page, size, sort)
}

private fun toCriteria(condition: Condition?): Criteria {
    val predicates = condition?.predicates?.toList()
    return if (predicates.isNullOrEmpty()) {
        Criteria.where("id").isNotNull
    }
    else {
        val criteria = Criteria.where(predicates[0].field).accept(predicates[0])
        predicates.subList(1, predicates.size).fold(criteria) { acc, p -> acc.accept(p) }
    }
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