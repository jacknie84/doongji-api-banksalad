package com.jacknie.doongji.banksalad.endpoint

import com.jacknie.doongji.banksalad.model.UploadFile
import com.jacknie.doongji.banksalad.model.UploadFileRepository
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.queryParamOrNull
import reactor.core.publisher.Mono

class UploadFileHandler(private val uploadFileRepository: UploadFileRepository) {

    fun put(request: ServerRequest): Mono<out ServerResponse> {
        val uploadPath = request.pathVariable("uploadPath")
        val contentLength = request.headers().contentLength().orElse(0L)
        val contentType = request.headers().contentType().orElse(MediaType.APPLICATION_OCTET_STREAM)
        val filename = extractFilenameAtQueryParam(request)?: extractFilenameAtHttpHeaders(request)?: "unknown"
        val uploadFile = UploadFile(path = uploadPath, filename = filename, filesize = contentLength, mimeType = contentType.toString())
        return uploadFileRepository.save(uploadFile)
                .flatMap { ServerResponse.ok().bodyValue(it) }
    }

    fun get(request: ServerRequest): Mono<out ServerResponse> {
        val uploadId = request.pathVariable("uploadId").toLong()
        return uploadFileRepository.findById(uploadId)
                .flatMap { ServerResponse.ok().bodyValue(it) }
    }

    private fun extractFilenameAtHttpHeaders(request: ServerRequest): String? {
        val headerParts = request.headers().header(HttpHeaders.CONTENT_DISPOSITION)
        return if (headerParts.isNotEmpty()) {
            val contentDisposition = ContentDisposition.parse(headerParts.joinToString(";"))
            contentDisposition.filename
        }
        else {
            null
        }
    }

    private fun extractFilenameAtQueryParam(request: ServerRequest): String? {
        return request.queryParamOrNull("filename")
    }
}