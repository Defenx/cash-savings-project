plugins { }

allprojects {
	group = "com.kavencore"
	version = "0.0.1-SNAPSHOT"
}

subprojects {
	repositories { mavenCentral() }


	plugins.withId("java") {
		the<JavaPluginExtension>().toolchain {
			languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
		}
		tasks.withType<Test>().configureEach {
			useJUnitPlatform()

			// Это исправляет проблемы с кодировкой в консоли Windows
			jvmArgs("-Dfile.encoding=UTF-8")

			// Это заставляет тесты работать в одном процессе с Gradle,
			// что решает ошибку "Could not find or load main class"
			setForkEvery(0)
		}
	}
	configurations.configureEach {
		resolutionStrategy.force(libs.commons.compress)
	}
}