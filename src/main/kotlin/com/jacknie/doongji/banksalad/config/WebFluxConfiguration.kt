package com.jacknie.doongji.banksalad.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.jacknie.doongji.banksalad.endpoint.*
import com.jacknie.doongji.banksalad.model.*
import com.jacknie.filestore.FileStoreTemplate
import com.jacknie.filestore.filesystem.FileSystemStoreSession
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router


@Configuration
@EnableWebFlux
class WebFluxConfiguration: WebFluxConfigurer {

    private val version = "v1"
    @Autowired private lateinit var dslContext: DSLContext
    @Autowired private lateinit var client: DatabaseClient
    @Autowired private lateinit var modelRepository: ModelRepository
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var uploadFileRepository: UploadFileRepository
    @Autowired private lateinit var fileStore: FileStoreTemplate<FileSystemStoreSession>
    @Autowired private lateinit var sharedExcelRepository: SharedExcelRepository
    @Autowired private lateinit var householdAccountsRepository: HouseholdAccountsRepository
    @Autowired private lateinit var retrievedConditionRepository: RetrievedConditionRepository
    @Autowired private lateinit var retrievedConditionPredicateRepository: RetrievedConditionPredicateRepository
    @Bean fun uploadFileHandler() = UploadFileHandler(uploadFileRepository, fileStore)
    @Bean fun downloadFileHandler() = DownloadFileHandler(uploadFileRepository, fileStore)
    @Bean fun sharedExcelHandler() = SharedExcelsHandler(sharedExcelRepository)
    @Bean fun sharedExcelContentHandler() = SharedExcelContentHandler(householdAccountsRepository)
    @Bean fun householdAccountsHandler() = HouseholdAccountsHandler(modelRepository, householdAccountsRepository, objectMapper)
    @Bean fun retrievedConditionHandler() = RetrievedConditionHandler(modelRepository, client, retrievedConditionRepository, retrievedConditionPredicateRepository, objectMapper)
    @Bean fun statisticsHandler() = StatisticsHandler(dslContext, client, objectMapper)

    @Bean
    fun v1RouterFunction() = router {
        version.nest {
            OPTIONS("/**") { ServerResponse.ok().build() }
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
            "/retrieved-conditions".nest {
                POST("", retrievedConditionHandler()::post)
                GET("", retrievedConditionHandler()::getList)
                GET("/{id}", retrievedConditionHandler()::get)
                PUT("/{id}", retrievedConditionHandler()::put)
                PUT("/{conditionId}/predicates", retrievedConditionHandler()::putPredicates)
                GET("/{conditionId}/predicates", retrievedConditionHandler()::getPredicates)
            }
            "/statistics".nest {
                GET("/{groupBy}", statisticsHandler()::getGroupBy)
            }
        }
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/$version/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .exposedHeaders("X-Total-Count", "Date", "Location")
                .allowCredentials(true)
                .maxAge(3600)
    }
}