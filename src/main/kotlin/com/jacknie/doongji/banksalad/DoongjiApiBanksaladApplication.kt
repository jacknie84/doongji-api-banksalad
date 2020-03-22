package com.jacknie.doongji.banksalad

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@EnableR2dbcRepositories
@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class DoongjiApiBanksaladApplication

fun main() {
    runApplication<DoongjiApiBanksaladApplication>()
}