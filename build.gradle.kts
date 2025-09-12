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
		tasks.withType<Test>().configureEach { useJUnitPlatform() }
	}
	configurations.configureEach {
		resolutionStrategy.force(libs.commons.compress)
	}
}