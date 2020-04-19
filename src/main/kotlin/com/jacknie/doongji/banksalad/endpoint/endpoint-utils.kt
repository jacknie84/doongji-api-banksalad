package com.jacknie.doongji.banksalad.endpoint

import org.springframework.data.domain.Page
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

fun extractFilenameAtHttpHeaders(request: ServerRequest): String? {
    val headerParts = request.headers().header(HttpHeaders.CONTENT_DISPOSITION)
    return if (headerParts.isNotEmpty()) {
        val contentDisposition = ContentDisposition.parse(headerParts.joinToString(";"))
        contentDisposition.filename
    }
    else {
        null
    }
}

fun <T> responseOf(page: Page<T>) = if (page.isEmpty) {
    ServerResponse.noContent()
            .headers { it.set("X-Total-Count", "0") }
            .build()
} else {
    ServerResponse.ok()
            .headers { it.set("X-Total-Count", page.totalElements.toString()) }
            .bodyValue(page.content)
}

fun <T> continuousMono(condition: Boolean, supplier: () -> Mono<T>): Mono<T> {
    return if (condition) {
        supplier.invoke()
    }
    else {
        Mono.empty()
    }
}

fun <T> continuousFlux(condition: Boolean, supplier: () -> Flux<T>): Flux<T> {
    return if (condition) {
        supplier.invoke()
    }
    else {
        Flux.empty()
    }
}