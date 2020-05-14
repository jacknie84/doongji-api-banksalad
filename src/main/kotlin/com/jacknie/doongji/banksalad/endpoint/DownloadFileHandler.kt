package com.jacknie.doongji.banksalad.endpoint

import com.jacknie.doongji.banksalad.model.UploadFileRepository
import com.jacknie.filestore.FileStoreTemplate
import com.jacknie.filestore.filesystem.FileSystemStoreSession
import com.jacknie.filestore.model.FilePath
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ContentDisposition
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

class DownloadFileHandler(
        private val uploadFileRepository: UploadFileRepository,
        private val fileStore: FileStoreTemplate<FileSystemStoreSession>) {

    fun get(request: ServerRequest): Mono<out ServerResponse> {
        val uploadId = request.pathVariable("uploadId").toLong()
        return uploadFileRepository.findById(uploadId)
                .map { Pair(it, fileStore.load(FilePath.parse("/", it.path))) }
                .map { Pair(it.first, InputStreamResource(it.second)) }
                .flatMap { (first, second) -> ServerResponse.ok().headers {
                    it.contentDisposition = ContentDisposition.builder("attachment")
                            .filename(first.filename, StandardCharsets.UTF_8)
                            .build()
                    it.contentType = MediaType.APPLICATION_OCTET_STREAM
                }.bodyValue(second) }
                .switchIfEmpty(ServerResponse.notFound().build())
    }
}