plugins {
	java
	alias(libs.plugins.spring.boot)
	alias(libs.plugins.openapi.generator)
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
	implementation(libs.spring.boot.starter.webmvc.ui)

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
	implementation(libs.liquibase.core)

	// Dev-only (оставь, если используешь)
	developmentOnly(libs.spring.boot.devtools)
	developmentOnly(libs.spring.boot.docker.compose)

	// Tests
	testImplementation(libs.spring.boot.starter.test)
	testImplementation(libs.testcontainers)
	testImplementation(libs.testcontainers.junit)
	testImplementation(libs.testcontainers.postgresql)
	testImplementation(libs.spring.boot.testcontainers)

	constraints {
		implementation(libs.commons.compress) { because("Fix CVE-2024-25710, CVE-2024-26308") }
		testImplementation(libs.commons.compress)
	}
}
val generatedRoot = layout.buildDirectory.dir("generated/accounts")

openApiGenerate {
	generatorName.set("spring")
	inputSpec.set("$rootDir/openapi.yaml")
	outputDir.set(generatedRoot.get().asFile.absolutePath)

	apiPackage.set("com.kavencore.moneyharbor.app.api.controller")
	modelPackage.set("com.kavencore.moneyharbor.app.api.model")

	configOptions.set(
		mapOf(
			"useSpringBoot3" to "true",
			"useJakartaEe" to "true",
			"openApiNullable" to "false",
			"useBeanValidation" to "true",
			"useBigDecimal" to "true",
			"dateLibrary" to "java8",
			"hideGenerationTimestamp" to "true",
			"serializableModel" to "true",
			"library" to "spring-boot",
			"useSwaggerAnnotations" to "true",
			"sourceFolder" to "src/main/java",
			"interfaceOnly" to "true"
		)
	)

	additionalProperties.set(
		mapOf(
			"modelNameSuffix" to "Dto"
		)
	)

	typeMappings.set(mapOf("number" to "BigDecimal"))
	importMappings.set(mapOf("BigDecimal" to "java.math.BigDecimal"))
}

tasks.named("openApiGenerate") {
	doFirst {
		delete(generatedRoot)
	}
}

sourceSets {
	val genJava = generatedRoot.map { it.dir("src/main/java") }
	val genRes  = generatedRoot.map { it.dir("src/main/resources") }

	named("main") {
		java.srcDir(genJava)
	}
}
tasks.named("compileJava") {
	dependsOn("openApiGenerate")
}

tasks.matching { it.name == "bootRun" }.configureEach {
	dependsOn("openApiGenerate")
}

tasks.withType<Test> { useJUnitPlatform() }



