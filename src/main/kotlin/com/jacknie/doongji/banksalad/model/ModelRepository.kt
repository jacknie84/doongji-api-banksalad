package com.jacknie.doongji.banksalad.model

import com.jacknie.doongji.banksalad.endpoint.condition
import com.jacknie.doongji.banksalad.endpoint.pageable
import com.jacknie.doongji.banksalad.endpoint.resolvePageable
import com.jacknie.doongji.banksalad.endpoint.selectedFields
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.conf.ParamType
import org.jooq.impl.TableImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.asType
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class ModelRepository {

    @Autowired private lateinit var dslContext: DSLContext
    @Autowired private lateinit var client: DatabaseClient

    fun <T> findAll(table: TableImpl<out Record>, selector: Selector, type: Class<T>): Mono<Page<T>> {
        val selectFields = table.selectedFields(selector)
        val pageable = resolvePageable(selector)
        var countQuery = dslContext
                .selectCount()
                .from(table)
        var listQuery = dslContext
                .select(selectFields)
                .from(table)
        if (!selector.condition?.predicates.isNullOrEmpty()) {
            val condition = table.condition(selector)
            countQuery.where(condition)
            listQuery.where(condition)
        }
        val countSql = countQuery.getSQL(ParamType.INLINED)
        val listSql = listQuery.pageable(pageable, table).getSQL(ParamType.INLINED)
        return client.execute(countSql).asType<Long>()
                .fetch().one()
                .filter { it > 0 }
                .zipWith(client.execute(listSql).`as`(type)
                        .fetch().all()
                        .collectList())
                .map { PageImpl(it.t2, pageable, it.t1) }
    }
}