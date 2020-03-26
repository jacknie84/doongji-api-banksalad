package com.jacknie.doongji.banksalad.config

import com.jacknie.doongji.banksalad.endpoint.UploadFileHandler
import com.jacknie.doongji.banksalad.model.UploadFileRepository
import com.jacknie.fd.FileDelivery
import com.jacknie.fd.fs.FsFileStoreSession
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.router

@Configuration
@EnableWebFlux
class WebFluxConfiguration: WebFluxConfigurer {

    private val version = "v1"

    @Autowired
    private lateinit var uploadFileRepository: UploadFileRepository

    @Autowired
    private lateinit var fileDelivery: FileDelivery<FsFileStoreSession>

    @Bean
    fun v1RouterFunction(): RouterFunction<*> {

        val uploadFileHandler = UploadFileHandler(uploadFileRepository, fileDelivery)

        return router {
            version.nest {
                "/upload".nest {
                    PUT("/{*uploadPath}", uploadFileHandler::put)
                    GET("/{uploadId}", uploadFileHandler::get)
                }
            }
        }
    }
}