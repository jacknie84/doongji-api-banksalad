package com.jacknie.doongji.banksalad.model

data class StatisticsAmount (
        val group: String,
        val totalAmount: Long
)

enum class GroupBy{
    TOTAL,
    YEAR,
    MONTH,
    DAY,
    HOUR,
    DAY_OF_WEEK,
    TYPE,
    CATEGORY,
    SUB_CATEGORY,
    USE_CURRENCY,
    USE_OBJECT,
    USER_ID
}
