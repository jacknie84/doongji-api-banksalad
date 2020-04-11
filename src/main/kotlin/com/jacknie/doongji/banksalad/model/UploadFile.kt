package com.jacknie.doongji.banksalad.model

import org.springframework.data.annotation.Id
import org.springframework.data.repository.reactive.ReactiveCrudRepository

data class UploadFile(

        @Id
        var id: Long? = null,
        var path: String,
        var filename: String,
        var filesize: Long,
        var mimeType: String
)

interface UploadFileRepository: ReactiveCrudRepository<UploadFile, Long>