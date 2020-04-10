package com.jacknie.doongji.banksalad.config

import com.jacknie.doongji.banksalad.endpoint.DownloadFileHandler
import com.jacknie.doongji.banksalad.endpoint.SharedExcelContentHandler
import com.jacknie.doongji.banksalad.endpoint.SharedExcelsHandler
import com.jacknie.doongji.banksalad.endpoint.UploadFileHandler
import com.jacknie.doongji.banksalad.model.HouseholdAccountsRepository
import com.jacknie.doongji.banksalad.model.SharedExcelRepository
import com.jacknie.doongji.banksalad.model.UploadFileRepository
import com.jacknie.fd.FileDelivery
import com.jacknie.fd.fs.FsFileStoreSession
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.router

@Configuration
@EnableWebFlux
class WebFluxConfiguration: WebFluxConfigurer {

    private val version = "v1"
    @Autowired private lateinit var uploadFileRepository: UploadFileRepository
    @Autowired private lateinit var fileDelivery: FileDelivery<FsFileStoreSession>
    @Autowired private lateinit var sharedExcelRepository: SharedExcelRepository
    @Autowired private lateinit var householdAccountsRepository: HouseholdAccountsRepository
    @Bean fun uploadFileHandler() = UploadFileHandler(uploadFileRepository, fileDelivery)
    @Bean fun downloadFileHandler() = DownloadFileHandler(uploadFileRepository, fileDelivery)
    @Bean fun sharedExcelsHandler() = SharedExcelsHandler(sharedExcelRepository)
    @Bean fun sharedExcelContentHandler() = SharedExcelContentHandler(householdAccountsRepository)

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
                POST("", sharedExcelsHandler()::post)
                GET("/{id}", sharedExcelsHandler()::get)
                PUT("/{userId}/contents", sharedExcelContentHandler()::put)
                GET("/{userId}/contents", sharedExcelContentHandler()::get)
            }
        }
    }
}