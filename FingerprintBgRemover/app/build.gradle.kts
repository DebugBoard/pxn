plugins {
	id("com.android.application")
}

android {
	namespace = "sh.siava.fingerprintbg"
	compileSdk = 36

	defaultConfig {
		applicationId = "sh.siava.fingerprintbg"
		minSdk = 31
		targetSdk = 36
		versionCode = 1
		versionName = "1.0"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			//so that a plain "assembleRelease" produces an installable APK
			signingConfig = signingConfigs.getByName("debug")
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
}

dependencies {
	compileOnly("de.robv.android.xposed:api:82")
}
