package com.jacknie.doongji.banksalad.model

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RetrievedCondition(

        @Id
        var id: Long? = null,
        var name: String? = null,
        var favorite: Boolean? = null,
        var lastRetrievedDate: String? = null
)

interface RetrievedConditionRepository: ReactiveCrudRepository<RetrievedCondition, Long>

data class RetrievedConditionPredicate(

        @Id
        var id: Long? = null,
        var conditionId: Long?,
        var fieldName: String,
        var operator: Operator,
        var fieldValues: String? = null
)

interface RetrievedConditionPredicateRepository: ReactiveCrudRepository<RetrievedConditionPredicate, Long> {

    @Query("select * from doongji_retrieved_condition_predicate p where p.condition_id = $1")
    fun findByConditionId(conditionId: Long): Flux<RetrievedConditionPredicate>
}