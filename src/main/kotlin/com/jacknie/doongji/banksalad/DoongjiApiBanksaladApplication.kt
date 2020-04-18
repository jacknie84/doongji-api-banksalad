package com.jacknie.doongji.banksalad

import com.jacknie.fd.FileDelivery
import com.jacknie.fd.fs.FsFileDeliveryFactory
import com.jacknie.fd.fs.FsFileStoreSession
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import java.nio.file.FileSystems

@EnableR2dbcRepositories
@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class DoongjiApiBanksaladApplication {

    @Bean fun fileDelivery(): FileDelivery<FsFileStoreSession> {
        return FsFileDeliveryFactory(FileSystems.getDefault()).createFileDelivery()
    }
}

fun main() {
    runApplication<DoongjiApiBanksaladApplication>()
}