package com.jacknie.doongji.banksalad.model

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity(name = ENTITY_PREFIX + "UploadFile")
data class UploadFile(

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long,

        @Column(length = 4096, nullable = false)
        var path: String,

        @Column(length = 4096, nullable = false)
        var filename: String,

        @Column(nullable = false)
        var filesize: Long,

        @Column(nullable = false)
        var mimeType: String

): Auditable()

interface UploadFileRepository: JpaRepository<UploadFile, Long>