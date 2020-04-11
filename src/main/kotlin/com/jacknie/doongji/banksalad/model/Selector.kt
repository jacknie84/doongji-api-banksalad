package com.jacknie.doongji.banksalad.model

import org.springframework.data.domain.Sort

data class Selector(
        var selectedFields: List<String>,
        var pagination: Pagination?,
        var condition: Condition
)

data class Pagination(
        var page: Int,
        var size: Int,
        var orders: Set<Order>?
)

data class Order(
        var direction: Sort.Direction,
        var property: String
)

data class Condition(

        var predicates: Set<Predicate>
)

data class Predicate(

        var field: String,
        var operator: Operator,
        var values: List<String>
)

enum class Operator {
    IS_NULL,
    IS_NOT_NULL,
    EQUALS,
    NOT_EQUALS,
    IN,
    NOT_IN,
    GREATER_THAN,
    GREATER_THAN_EQUALS,
    LESS_THAN,
    LESS_THAN_EQUALS,
    STARTS_WITH,
    ENDS_WITH,
    CONTAINS
}