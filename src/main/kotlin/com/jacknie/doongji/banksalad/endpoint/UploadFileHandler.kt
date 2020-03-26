package com.jacknie.doongji.banksalad.endpoint

import com.jacknie.doongji.banksalad.config.FilePolicyImpl
import com.jacknie.doongji.banksalad.model.UploadFile
import com.jacknie.doongji.banksalad.model.UploadFileRepository
import com.jacknie.fd.FileDelivery
import com.jacknie.fd.FileSource
import com.jacknie.fd.fs.FsFileStoreSession
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.StringUtils
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.queryParamOrNull
import reactor.core.publisher.Mono
import java.io.SequenceInputStream
import java.util.*

class UploadFileHandler(
        private val uploadFileRepository: UploadFileRepository,
        private val fileDelivery: FileDelivery<FsFileStoreSession>) {

    fun put(request: ServerRequest): Mono<out ServerResponse> {
        val uploadPath = request.pathVariable("uploadPath").substring(1)
        val contentLength = request.headers().contentLength().orElse(0L)
        val contentType = request.headers().contentType().orElse(MediaType.APPLICATION_OCTET_STREAM)
        val filename = extractFilenameAtQueryParam(request)?: extractFilenameAtHttpHeaders(request)?: "unknown"
        return request.bodyToFlux(DataBuffer::class.java)
                .map { it.asInputStream() }
                .collectList()
                .map { SequenceInputStream(Collections.enumeration(it)) }
                .map { FileSource(
                        content = it,
                        extension = StringUtils.getFilenameExtension(filename).orEmpty(),
                        storePath = uploadPath,
                        filename = filename,
                        filesize = contentLength,
                        mimeType = contentType.toString()
                ) }
                .map { fileDelivery.put(FilePolicyImpl(), it) }
                .flatMap { uploadFileRepository.save(UploadFile(
                        path = it.run {"$path/$filename"},
                        filename = it.originalFilename,
                        mimeType = it.mimeType.toString(),
                        filesize = it.filesize
                )) }
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