package com.jacknie.doongji.banksalad.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.jacknie.doongji.banksalad.model.GroupBy
import com.jacknie.doongji.banksalad.model.Selector
import com.jacknie.doongji.banksalad.model.StatisticsAmount
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.conf.ParamType
import org.jooq.generated.public_.Tables
import org.jooq.impl.DSL
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.asType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.queryParamOrNull
import reactor.core.publisher.Mono

class StatisticsHandler(
        private val dslContext: DSLContext,
        private val client: DatabaseClient,
        private val objectMapper: ObjectMapper
) {

    fun getGroupBy(request: ServerRequest): Mono<out ServerResponse> {
        val q = request.queryParamOrNull("q")?: "{}"
        val selector = objectMapper.readValue<Selector>(q)
        val table = Tables.DOONGJI_HOUSEHOLD_ACCOUNTS
        val groupByValue = request.pathVariable("groupBy")
        val groupBy = groupByValue.let { GroupBy.valueOf(it.toUpperCase().replace("-", "_")) }
        val group = resolveField(groupBy)
        val totalAmount = DSL.sum(table.USE_AMOUNT).`as`("total_amount")
        val query = dslContext
                .select(group, totalAmount)
                .from(table)
        if (!selector.condition?.predicates.isNullOrEmpty()) {
            val condition = table.condition(selector)
            query.where(condition)
        }
        val sql = query.groupBy(group).getSQL(ParamType.INLINED)
        return client.execute(sql).asType<StatisticsAmount>()
                .fetch().all()
                .collectList()
                .flatMap { ServerResponse.ok().bodyValue(it) }
    }

    private fun resolveField(groupBy: GroupBy): Field<String> {
        val useDate = Tables.DOONGJI_HOUSEHOLD_ACCOUNTS.USE_DATE
        val useTime = Tables.DOONGJI_HOUSEHOLD_ACCOUNTS.USE_TIME
        return when (groupBy) {
            GroupBy.TOTAL -> DSL.inline("TOTAL")
            GroupBy.YEAR -> DSL.substring(useDate, 0, 4)
            GroupBy.MONTH -> DSL.substring(useDate, 6, 2)
            GroupBy.DAY -> DSL.substring(useDate, 9, 2)
            GroupBy.DAY_OF_WEEK -> DSL.case_(DSL.isoDayOfWeek(DSL.toDate(useDate, "yyyy-MM-dd")))
                    .`when`(1, "월")
                    .`when`(2, "화")
                    .`when`(3, "수")
                    .`when`(4, "목")
                    .`when`(5, "금")
                    .`when`(6, "토")
                    .`when`(7, "일")
                    .else_("에러")
            GroupBy.HOUR -> DSL.substring(useTime, 0, 2)
            else -> Tables.DOONGJI_HOUSEHOLD_ACCOUNTS.field(groupBy.name, String::class.java)
        }.`as`("group")
    }
}