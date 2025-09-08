plugins {
	java
	alias(libs.plugins.spring.boot)
}

group = "com.kavencore"
version = "0.0.1-SNAPSHOT"
description = "Spring Boot application for managing expenses, incomes, and budgets"

java {
	toolchain { languageVersion = JavaLanguageVersion.of(libs.versions.java.get()) }
}

repositories { mavenCentral() }

dependencies {
	// Импорт BOM (даёт версии всем spring-boot-* и их транзитивным)
	implementation(platform(libs.spring.boot.bom))
	annotationProcessor(platform(libs.spring.boot.bom))
	developmentOnly(platform(libs.spring.boot.bom))

	// Стартеры
	implementation(libs.spring.boot.starter.web)
	implementation(libs.spring.boot.starter.data.jpa)
	implementation(libs.spring.boot.starter.validation)

	// Config props подсказки в IDE
	annotationProcessor(libs.spring.boot.configuration.processor)

	// Lombok
	compileOnly(libs.lombok)
	annotationProcessor(libs.lombok)
	testCompileOnly(libs.lombok)
	testAnnotationProcessor(libs.lombok)

	// MapStruct
	implementation(libs.mapstruct)
	annotationProcessor(libs.mapstruct.processor)
	testAnnotationProcessor(libs.mapstruct.processor)
	annotationProcessor(libs.lombok.mapstruct.binding)
	testAnnotationProcessor(libs.lombok.mapstruct.binding)

	// DB
	runtimeOnly(libs.postgresql)

	// Dev-only (оставь, если используешь)
	developmentOnly(libs.spring.boot.devtools)
	developmentOnly(libs.spring.boot.docker.compose)

	// Tests
	testImplementation(libs.spring.boot.starter.test)
	testImplementation(libs.testcontainers)
	testImplementation(libs.testcontainers.junit)
	testImplementation(libs.testcontainers.postgresql)

	constraints {
		implementation(libs.commons.compress) { because("Fix CVE-2024-25710, CVE-2024-26308") }
		testImplementation(libs.commons.compress)
	}
}

tasks.withType<Test> { useJUnitPlatform() }

