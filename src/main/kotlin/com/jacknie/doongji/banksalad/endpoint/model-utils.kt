@file:Suppress("UNCHECKED_CAST")

package com.jacknie.doongji.banksalad.endpoint

import com.jacknie.doongji.banksalad.model.Operator
import com.jacknie.doongji.banksalad.model.Pagination
import com.jacknie.doongji.banksalad.model.Predicate
import com.jacknie.doongji.banksalad.model.Selector
import org.jooq.*
import org.jooq.generated.public_.tables.records.DoongjiHouseholdAccountsRecord
import org.jooq.impl.TableImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.math.BigDecimal

fun TableImpl<out Record>.selectedFields(selector: Selector): Collection<SelectFieldOrAsterisk> = selector
        .selectedFields
        ?.map { field(it.toSnakeCase().toUpperCase()) }
        ?: emptyList()

fun TableImpl<out Record>.condition(selector: Selector): Condition {
    val predicates = selector.condition?.predicates?: emptySet()
    val conditions = predicates.map { condition(it) }
    return conditions.reduce(Condition::and)
}

fun TableImpl<out Record>.condition(predicate: Predicate): Condition {
    val (field, operator, values) = predicate
    val tableField = field(field.toSnakeCase().toUpperCase())
    return when (tableField.type) {
        Long::class.java -> resolveLongCondition(tableField as TableField<DoongjiHouseholdAccountsRecord, Long>,
                operator, values.map { it.toLong() })
        BigDecimal::class.java -> resolveBigDecimalCondition(tableField as TableField<DoongjiHouseholdAccountsRecord, BigDecimal>,
                operator, values.map { it.toBigDecimal() })
        else -> resolveStringCondition(tableField as TableField<DoongjiHouseholdAccountsRecord, String>,
                operator, values)
    }
}

fun SelectOrderByStep<out Record>.pageable(pageable: Pageable, table: TableImpl<out Record>): SelectOrderByStep<out Record> {
    if (pageable.sort.isSorted) {
        pageable.sort.forEach {
            val tableField = table.field(it.property.toSnakeCase().toUpperCase())
            if (it.isDescending) {
                orderBy(tableField.desc())
            } else {
                orderBy(tableField.asc())
            }
        }
    }
    if (pageable.isPaged) {
        offset(pageable.offset).limit(pageable.pageSize)
    }
    return this
}

fun resolvePageable(selector: Selector): Pageable {
    val (page, size, orders) = selector.pagination?: Pagination(1, 10, null)
    val sort = Sort.by(orders?.map { Sort.Order(it.direction, it.property) }?: emptyList())
    return PageRequest.of(if (page > 0) page - 1 else page, size, sort)
}

fun resolveLongCondition(tableField: TableField<DoongjiHouseholdAccountsRecord, Long>, operator: Operator, values: List<Long>): Condition {
    return when(operator) {
        Operator.IS_NULL -> tableField.isNull
        Operator.IS_NOT_NULL -> tableField.isNotNull
        Operator.EQUALS -> if (values.size == 1) {
            tableField.eq(values[0])
        } else {
            tableField.`in`(values)
        }
        Operator.NOT_EQUALS -> if (values.size == 1) {
            tableField.ne(values[0])
        } else {
            tableField.notIn(values)
        }
        Operator.IN -> tableField.`in`(values)
        Operator.NOT_IN -> tableField.notIn(values)
        Operator.GREATER_THAN -> values.map { tableField.gt(it) }.reduce(Condition::or)
        Operator.GREATER_THAN_EQUALS -> values.map { tableField.ge(it) }.reduce(Condition::or)
        Operator.LESS_THAN -> values.map { tableField.lt(it) }.reduce(Condition::or)
        Operator.LESS_THAN_EQUALS -> values.map { tableField.le(it) }.reduce(Condition::or)
        else -> throw IllegalStateException("unsupported operator: $operator")
    }
}

fun resolveBigDecimalCondition(tableField: TableField<DoongjiHouseholdAccountsRecord, BigDecimal>, operator: Operator, values: List<BigDecimal>): Condition {
    return when(operator) {
        Operator.IS_NULL -> tableField.isNull
        Operator.IS_NOT_NULL -> tableField.isNotNull
        Operator.EQUALS -> if (values.size == 1) {
            tableField.eq(values[0])
        } else {
            tableField.`in`(values)
        }
        Operator.NOT_EQUALS -> if (values.size == 1) {
            tableField.ne(values[0])
        } else {
            tableField.notIn(values)
        }
        Operator.IN -> tableField.`in`(values)
        Operator.NOT_IN -> tableField.notIn(values)
        Operator.GREATER_THAN -> values.map { tableField.gt(it) }.reduce(Condition::or)
        Operator.GREATER_THAN_EQUALS -> values.map { tableField.ge(it) }.reduce(Condition::or)
        Operator.LESS_THAN -> values.map { tableField.lt(it) }.reduce(Condition::or)
        Operator.LESS_THAN_EQUALS -> values.map { tableField.le(it) }.reduce(Condition::or)
        else -> throw IllegalStateException("unsupported operator: $operator")
    }
}

fun resolveStringCondition(tableField: TableField<DoongjiHouseholdAccountsRecord, String>, operator: Operator, values: List<String>): Condition {
    return when(operator) {
        Operator.IS_NULL -> tableField.isNull
        Operator.IS_NOT_NULL -> tableField.isNotNull
        Operator.EQUALS -> if (values.size == 1) {
            tableField.eq(values[0])
        } else {
            tableField.`in`(values)
        }
        Operator.NOT_EQUALS -> if (values.size == 1) {
            tableField.ne(values[0])
        } else {
            tableField.notIn(values)
        }
        Operator.IN -> tableField.`in`(values)
        Operator.NOT_IN -> tableField.notIn(values)
        Operator.CONTAINS -> values.map { tableField.containsIgnoreCase(it) }.reduce(Condition::or)
        Operator.STARTS_WITH -> values.map { tableField.startsWithIgnoreCase(it) }.reduce(Condition::or)
        Operator.ENDS_WITH -> values.map { tableField.endsWithIgnoreCase(it) }.reduce(Condition::or)
        else -> throw IllegalStateException("unsupported operator: $operator")
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