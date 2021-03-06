package com.jacknie.doongji.banksalad.endpoint

import com.jacknie.doongji.banksalad.model.HouseholdAccounts
import com.jacknie.doongji.banksalad.model.HouseholdAccountsRepository
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

class SharedExcelContentHandler(private val householdAccountsRepository: HouseholdAccountsRepository) {

    fun put(request: ServerRequest): Mono<out ServerResponse> {
        val userId = request.pathVariable("userId")
        return request.bodyToFlux(HouseholdAccounts::class.java)
                .flatMap {
                    householdAccountsRepository.findByRecord(it.useDate, it.useTime, it.useAmount, it.useCurrency, it.useObject, userId)
                            .map { record ->
                                record.type = it.type
                                record.category = it.category
                                record.subCategory = it.subCategory
                                record.description = it.description
                                record
                            }
                            .switchIfEmpty(Mono.just(it))
                }
                .flatMap { householdAccountsRepository.save(it) }
                .then(ServerResponse.noContent().build())
    }

    fun get(request: ServerRequest): Mono<out ServerResponse> {
        val userId = request.pathVariable("userId")
        return householdAccountsRepository.findByUserId(userId)
                .collectList()
                .flatMap { ServerResponse.ok().bodyValue(it) }
    }

}