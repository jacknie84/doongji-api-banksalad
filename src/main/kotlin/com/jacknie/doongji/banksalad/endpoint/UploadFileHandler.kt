package com.jacknie.doongji.banksalad.endpoint

import com.jacknie.doongji.banksalad.model.UploadFile
import com.jacknie.doongji.banksalad.model.UploadFileRepository
import com.jacknie.filestore.FileStoreSession
import com.jacknie.filestore.FileStoreTemplate
import com.jacknie.filestore.filesystem.FileSystemStoreSession
import com.jacknie.filestore.model.*
import com.jacknie.filestore.support.FileStoreLocatorBuilder
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono
import java.io.SequenceInputStream
import java.time.LocalDate
import java.util.*

class UploadFileHandler(
        private val uploadFileRepository: UploadFileRepository,
        private val fileStore: FileStoreTemplate<FileSystemStoreSession>) {

    private val uploadPolicy = UploadPolicy(
            filesizeLimit = 10 * 1024 * 1024,
            filenameExtCaseSensitive = false,
            allowedMimeTypes = setOf(".+".toRegex()),
            allowedFilenameExts = setOf("xlsx")
    )

    fun put(request: ServerRequest): Mono<out ServerResponse> {
        val uploadPath = request.pathVariable("uploadPath").substring(1)
        val contentLength = request.headers().contentLengthOrNull()?: 0L
        val contentType = request.headers().contentTypeOrNull()?: MediaType.APPLICATION_OCTET_STREAM
        val filename = Filename(request.queryParamOrNull("filename")?: extractFilenameAtHttpHeaders(request)?: "unknown")
        return request.bodyToFlux(DataBuffer::class.java)
                .map { it.asInputStream() }
                .collectList()
                .map { SequenceInputStream(Collections.enumeration(it)) }
                .map { UploadSource(
                        content = it,
                        filename = filename,
                        mimType = contentType.toString(),
                        filesize = contentLength
                ) }
                .map { fileStore.save(it, uploadPolicy, FileStoreLocatorBuilder()
                        .uploadDir(FileDirectory(uploadPath).resolve(LocalDate.now().toString()))
                        .naming { Filename(UUID.randomUUID().toString(), filename.extension) }
                        .build())
                }
                .map { UploadFile(
                        path = it.toString(),
                        filename = filename.toString(),
                        mimeType = contentType.toString(),
                        filesize = contentLength
                ) }
                .flatMap { uploadFileRepository.save(it) }
                .flatMap { ServerResponse.ok().bodyValue(it) }
    }

    fun get(request: ServerRequest): Mono<out ServerResponse> {
        val uploadId = request.pathVariable("uploadId").toLong()
        return uploadFileRepository.findById(uploadId)
                .flatMap { ServerResponse.ok().bodyValue(it) }
    }

}
