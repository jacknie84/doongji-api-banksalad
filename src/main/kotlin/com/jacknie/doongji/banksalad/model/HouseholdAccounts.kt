package com.jacknie.doongji.banksalad.model

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@JsonInclude(JsonInclude.Include.NON_NULL)
data class HouseholdAccounts(

        @Id
        var id: Long? = null,
        var useDate: String?,
        var useTime: String?,
        var type: String?,
        var category: String?,
        var subCategory: String?,
        var description: String?,
        var useAmount: Int?,
        var useCurrency: String?,
        var useObject: String?,
        var userId: String? = null
)

interface HouseholdAccountsRepository: ReactiveCrudRepository<HouseholdAccounts, Long> {

        @Query("""
                select count(*) > 0
                from doongji_household_accounts h
                where h.use_date = $1
                and h.use_time = $2
                and h.use_amount = $3
                and h.use_currency = $4
                and h.use_object = $5
                and h.user_id = $6
        """)
        fun existsByRecord(useDate: String?, useTime: String?, useAmount: Int?, useCurrency: String?, useObject: String?, userId: String): Mono<Boolean>

        @Query("select * from doongji_household_accounts h where h.user_id = $1")
        fun findByUserId(userId: String): Flux<HouseholdAccounts>
}