package com.jacknie.doongji.banksalad.endpoint

import com.jacknie.doongji.banksalad.model.UploadFileRepository
import com.jacknie.fd.DeliveredFile
import com.jacknie.fd.FileDelivery
import com.jacknie.fd.fs.FsFileStoreSession
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ContentDisposition
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.nio.file.Paths

class DownloadFileHandler(
        private val uploadFileRepository: UploadFileRepository,
        private val fileDelivery: FileDelivery<FsFileStoreSession>) {

    fun get(request: ServerRequest): Mono<out ServerResponse> {
        val uploadId = request.pathVariable("uploadId").toLong()
        return uploadFileRepository.findById(uploadId)
                .map { Pair(it, Paths.get(it.path)) }
                .map { DeliveredFile(
                        path = "${it.second.parent?: "/"}",
                        filename = "${it.second.fileName?: "unknown"}",
                        filesize = it.first.filesize,
                        originalFilename = it.first.filename,
                        mimeType = it.first.mimeType,
                        extension = it.second.toFile().extension
                ) }
                .map { Pair(it, fileDelivery.get(it)) }
                .map { Pair(it.first, InputStreamResource(it.second)) }
                .flatMap { (first, second) -> ServerResponse.ok().headers {
                    it.contentDisposition = ContentDisposition.builder("attachment")
                            .filename(first.originalFilename, StandardCharsets.UTF_8)
                            .build()
                    it.contentType = MediaType.APPLICATION_OCTET_STREAM
                }.bodyValue(second) }
                .switchIfEmpty(ServerResponse.notFound().build())
    }
}