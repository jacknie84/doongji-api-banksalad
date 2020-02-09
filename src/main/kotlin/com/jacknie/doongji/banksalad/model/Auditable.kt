package com.jacknie.doongji.banksalad.model

import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import javax.persistence.Column
import javax.persistence.EntityListeners
import javax.persistence.MappedSuperclass

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
open class Auditable {

    @CreatedDate
    @Column(insertable=true, updatable=false)
    @ColumnDefault("now()")
    var createdDate: Instant = Instant.now()

    @CreatedBy
    @Column(insertable=true, updatable=false)
    @ColumnDefault("'system'")
    var createdBy: String = "anonymousUser"

    @LastModifiedDate
    @Column(insertable=true, updatable=true)
    @ColumnDefault("now()")
    var lastModifiedDate: Instant = Instant.now()

    @LastModifiedBy
    @Column(insertable=true, updatable=true)
    @ColumnDefault("'system'")
    var lastModifiedBy: String = "anonymousUser"
}