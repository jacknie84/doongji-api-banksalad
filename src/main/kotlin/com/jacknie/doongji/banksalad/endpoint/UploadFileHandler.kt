package com.jacknie.doongji.banksalad.endpoint

import com.jacknie.doongji.banksalad.config.FilePolicyImpl
import com.jacknie.doongji.banksalad.model.UploadFile
import com.jacknie.doongji.banksalad.model.UploadFileRepository
import com.jacknie.fd.FileDelivery
import com.jacknie.fd.FileSource
import com.jacknie.fd.fs.FsFileStoreSession
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.MediaType
import org.springframework.util.StringUtils
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono
import java.io.SequenceInputStream
import java.util.*

class UploadFileHandler(
        private val uploadFileRepository: UploadFileRepository,
        private val fileDelivery: FileDelivery<FsFileStoreSession>) {

    fun put(request: ServerRequest): Mono<out ServerResponse> {
        val uploadPath = request.pathVariable("uploadPath").substring(1)
        val contentLength = request.headers().contentLengthOrNull()?: 0L
        val contentType = request.headers().contentTypeOrNull()?: MediaType.APPLICATION_OCTET_STREAM
        val filename = request.queryParamOrNull("filename")?: extractFilenameAtHttpHeaders(request)?: "unknown"
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
                .map { UploadFile(
                        path = "${it.path}/${it.filename}",
                        filename = it.originalFilename,
                        mimeType = it.mimeType.toString(),
                        filesize = it.filesize
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