package com.jacknie.doongji.banksalad.endpoint

import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.server.ServerRequest

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