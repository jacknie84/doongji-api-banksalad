package com.jacknie.doongji.banksalad

import com.jacknie.filestore.FileStoreTemplate
import com.jacknie.filestore.filesystem.FileSystemStoreSession
import com.jacknie.filestore.filesystem.FileSystemStoreSessionFactory
import com.jacknie.filestore.model.FileDirectory
import com.jacknie.filestore.support.validateUploadSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import java.io.File
import java.nio.file.FileSystems

@EnableR2dbcRepositories
@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class DoongjiApiBanksaladApplication {

    @Bean fun fileStore(): FileStoreTemplate<FileSystemStoreSession> {
        val userHome = System.getProperty("user.home").replace(File.separator, "/")
        val baseDestPath = "${userHome}/doongji-api-banksalad"
        val storeFileDir = FileDirectory("/", baseDestPath)
        val factory = FileSystemStoreSessionFactory(storeFileDir, FileSystems.getDefault())
        return FileStoreTemplate(factory, ::validateUploadSource)
    }
}

fun main() {
    runApplication<DoongjiApiBanksaladApplication>()
}