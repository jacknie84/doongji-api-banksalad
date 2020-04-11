package com.jacknie.doongji.banksalad.endpoint

import com.jacknie.doongji.banksalad.model.SharedExcel
import com.jacknie.doongji.banksalad.model.SharedExcelRepository
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

class SharedExcelsHandler(private val sharedExcelRepository: SharedExcelRepository) {

    fun post(request: ServerRequest): Mono<out ServerResponse> {
        return request.bodyToMono(SharedExcel::class.java)
                .flatMap { sharedExcelRepository.save(it) }
                .doOnNext {  }
                .map { request.uriBuilder().path("/{id}").build(it.id) }
                .flatMap { ServerResponse.created(it).build() }
    }

    fun get(request: ServerRequest): Mono<out ServerResponse> {
        val id = request.pathVariable("id").toLong()
        return sharedExcelRepository.findById(id)
                .flatMap { ServerResponse.ok().bodyValue(it) }
    }
}