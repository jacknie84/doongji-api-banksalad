package com.jacknie.doongji.banksalad.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.jacknie.doongji.banksalad.endpoint.*
import com.jacknie.doongji.banksalad.model.HouseholdAccountsRepository
import com.jacknie.doongji.banksalad.model.SharedExcelRepository
import com.jacknie.doongji.banksalad.model.UploadFileRepository
import com.jacknie.fd.FileDelivery
import com.jacknie.fd.fs.FsFileStoreSession
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.router

@Configuration
@EnableWebFlux
class WebFluxConfiguration: WebFluxConfigurer {

    private val version = "v1"
    @Autowired private lateinit var client: DatabaseClient
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var uploadFileRepository: UploadFileRepository
    @Autowired private lateinit var fileDelivery: FileDelivery<FsFileStoreSession>
    @Autowired private lateinit var sharedExcelRepository: SharedExcelRepository
    @Autowired private lateinit var householdAccountsRepository: HouseholdAccountsRepository
    @Bean fun uploadFileHandler() = UploadFileHandler(uploadFileRepository, fileDelivery)
    @Bean fun downloadFileHandler() = DownloadFileHandler(uploadFileRepository, fileDelivery)
    @Bean fun sharedExcelHandler() = SharedExcelsHandler(sharedExcelRepository)
    @Bean fun sharedExcelContentHandler() = SharedExcelContentHandler(householdAccountsRepository)
    @Bean fun householdAccountsHandler() = HouseholdAccountsHandler(client, householdAccountsRepository, objectMapper)

    @Bean
    fun v1RouterFunction() = router {
        version.nest {
            "/upload".nest {
                PUT("/{*uploadPath}", uploadFileHandler()::put)
                GET("/{uploadId}", uploadFileHandler()::get)
            }
            "/download".nest {
                GET("/{uploadId}", downloadFileHandler()::get)
            }
            "/shared-excels".nest {
                POST("", sharedExcelHandler()::post)
                GET("/{id}", sharedExcelHandler()::get)
                PUT("/{userId}/contents", sharedExcelContentHandler()::put)
                GET("/{userId}/contents", sharedExcelContentHandler()::get)
            }
            "/household-accounts".nest {
                GET("", householdAccountsHandler()::getList)
                GET("/{id}", householdAccountsHandler()::get)
            }
        }
    }
}