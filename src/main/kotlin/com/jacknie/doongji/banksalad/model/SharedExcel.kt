package com.jacknie.doongji.banksalad.model

import org.springframework.data.annotation.Id
import org.springframework.data.repository.reactive.ReactiveCrudRepository

data class SharedExcel(

        @Id
        var id: Long? = null,
        var uploadId: Long,
        var userId: String
)

interface SharedExcelRepository: ReactiveCrudRepository<SharedExcel, Long>