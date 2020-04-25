plugins {
    id("org.springframework.boot") version "2.2.5.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("nu.studer.jooq") version "4.2"
    kotlin("jvm") version "1.3.61"
    kotlin("plugin.spring") version "1.3.61"
    kotlin("plugin.jpa") version "1.3.61"
}

group = "com.jacknie.doongji"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

val developmentOnly: Configuration by configurations.creating
configurations {
    runtimeClasspath {
        extendsFrom(developmentOnly)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    jooqRuntime("io.r2dbc:r2dbc-h2:0.8.2.RELEASE")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("io.projectreactor.addons:reactor-extra:3.3.3.RELEASE")
    implementation("org.springframework.data:spring-data-r2dbc:1.0.0.RELEASE")
    implementation("io.r2dbc:r2dbc-h2:0.8.2.RELEASE")
    implementation("io.r2dbc:r2dbc-pool:0.8.1.RELEASE")
    implementation("com.jacknie:file-delivery") {
        version {
            branch = "master"
        }
    }
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

jooq {
    val configuration = org.jooq.meta.jaxb.Configuration()
    val h2Config = nu.studer.gradle.jooq.JooqConfiguration("public", sourceSets["main"], configuration)
    whenConfigAdded.invoke(h2Config)
    configuration.apply {
        jdbc = org.jooq.meta.jaxb.Jdbc().apply {
            url = "jdbc:h2:~/h2/testdb"
            driver = "org.h2.Driver"
            user = "sa"
            password = ""
        }
        generator = org.jooq.meta.jaxb.Generator().apply {
            name = "org.jooq.codegen.DefaultGenerator"
            strategy = org.jooq.meta.jaxb.Strategy().apply {
                name = "org.jooq.codegen.DefaultGeneratorStrategy"
            }
            database = org.jooq.meta.jaxb.Database().apply {
                name = "org.jooq.meta.h2.H2Database"
                includes = ".*"
            }
            target = org.jooq.meta.jaxb.Target().apply {
                directory = "src/generated"
            }
        }
    }
}