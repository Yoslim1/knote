import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import java.io.File
import java.util.Properties
import java.util.regex.Pattern
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.androidx.baselineprofile)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) keystorePropertiesFile.inputStream().use { load(it) }
}

/**
 * Derives versionName/versionCode from the latest git tag (e.g. "v1.4.2" -> "1.4.2"),
 * instead of relying on a hand-edited constant that is easy to forget to bump before
 * tagging a release (this happened: v1.4.0 and v1.4.1 both shipped as versionCode 5,
 * which made Android treat them as the same version).
 *
 * Falls back to "0.0.0" / versionCode 1 if no tag is reachable (e.g. shallow checkout
 * without tags, or a fresh clone with no tags at all) so local/CI debug builds never fail.
 */
fun versionCodeFromTag(tag: String): Int {
    val parts = tag.split(".").map { it.toIntOrNull() ?: 0 }
    val major = parts.getOrElse(0) { 0 }
    val minor = parts.getOrElse(1) { 0 }
    val patch = parts.getOrElse(2) { 0 }
    return (major * 10_000 + minor * 100 + patch).coerceAtLeast(1)
}

// providers.exec is the configuration-cache-compatible way to run an external
// process at configuration time (ProcessBuilder/Runtime.exec are rejected by
// the configuration cache in Gradle 9.x). Runs in the script body so the
// Project receiver (providers) is resolvable.
val resolvedVersionName = try {
    providers.exec {
        commandLine("git", "describe", "--tags", "--abbrev=0")
        isIgnoreExitValue = true
    }.standardOutput.asText.get().trim().removePrefix("v").ifBlank { "0.0.0" }
} catch (e: Exception) {
    "0.0.0"
}
val resolvedVersionCode = versionCodeFromTag(resolvedVersionName)

android {
    lint {
        baseline = file("lint-baseline.xml")
    }

    namespace = "com.dalelalmuslim.knote"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.dalelalmuslim.knote"
        minSdk = 26
        targetSdk = 37
        versionCode = resolvedVersionCode
        versionName = resolvedVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    androidResources {
        localeFilters += listOf("ar", "en")
    }
}

composeCompiler {
    stabilityConfigurationFiles.add(layout.projectDirectory.file("compose_stability.conf"))
    if (project.findProperty("composeReports") == "true") {
        val dir = layout.buildDirectory.dir("compose_compiler")
        reportsDestination.set(dir)
        metricsDestination.set(dir)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

aboutLibraries {
    collect {
        fetchRemoteLicense = false
        fetchRemoteFunding = false
        includePlatform = true
    }
    export {
        prettyPrint = true
    }
    library {
        exclusionPatterns.set(
            setOf(Pattern.compile("^org\\.jetbrains\\.(compose|androidx)\\..*"))
        )
    }
}

val LEGAL_CLASSPATHS = listOf("releaseRuntimeClasspath", "debugRuntimeClasspath")

val legalNoticeNames  = listOf("NOTICE", "NOTICE.txt", "NOTICE.md")
val legalLicenseNames = listOf("LICENSE", "LICENSE.txt", "LICENSE.md")

fun zipEntryText(zip: ZipFile, names: List<String>): String? {
    for (candidate in names.flatMap { listOf("META-INF/$it", it) }) {
        val entry = zip.getEntry(candidate) ?: continue
        return zip.getInputStream(entry).bufferedReader(Charsets.UTF_8).use { it.readText() }.trim()
    }
    return null
}

fun nestedClassesJarText(zip: ZipFile, names: List<String>): String? {
    val classesJar = zip.getEntry("classes.jar") ?: return null
    val candidates = names.flatMap { listOf("META-INF/$it", it) }.toSet()
    ZipInputStream(zip.getInputStream(classesJar)).use { zis ->
        var entry = zis.nextEntry
        while (entry != null) {
            if (entry.name in candidates) return zis.bufferedReader(Charsets.UTF_8).readText().trim()
            entry = zis.nextEntry
        }
    }
    return null
}

fun extractLegal(artifact: File): Pair<String?, String?> {
    if (!artifact.exists() || (!artifact.name.endsWith(".jar") && !artifact.name.endsWith(".aar"))) return null to null
    return try {
        ZipFile(artifact).use { zip ->
            val notice  = zipEntryText(zip, legalNoticeNames)  ?: nestedClassesJarText(zip, legalNoticeNames)
            val license = zipEntryText(zip, legalLicenseNames) ?: nestedClassesJarText(zip, legalLicenseNames)
            notice to license
        }
    } catch (e: Exception) {
        null to null
    }
}

fun jsonQuote(s: String): String =
    "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"")
        .replace("\n", "\\n").replace("\r", "").replace("\t", "\\t") + "\""

tasks.register("collectLegalNotices") {
    group = "legal"
    description = "Extrahiert NOTICE/LICENSE aus allen Release-Artefakten nach res/raw/legal_notices.json"
    notCompatibleWithConfigurationCache("Löst Runtime-Classpaths zur Ausführungszeit auf")
    doLast {
        val resolved = LEGAL_CLASSPATHS
            .flatMap { configurations.getByName(it).resolvedConfiguration.resolvedArtifacts }
            .distinctBy { "${it.moduleVersion.id.group}:${it.moduleVersion.id.name}" }
        val entries = linkedMapOf<String, Map<String, String>>()
        resolved.sortedBy { "${it.moduleVersion.id.group}:${it.moduleVersion.id.name}" }
            .forEach { artifact ->
                val id = artifact.moduleVersion.id
                val (notice, license) = extractLegal(artifact.file)
                if (notice != null || license != null) {
                    entries["${id.group}:${id.name}"] = buildMap {
                        if (notice != null) put("notice", notice)
                        if (license != null) put("embeddedLicense", license)
                    }
                }
            }

        val sb = StringBuilder().append("{\n  \"notices\": {\n")
        entries.entries.forEachIndexed { i, (coord, fields) ->
            sb.append("    ${jsonQuote(coord)}: {\n")
            val list = fields.entries.toList()
            list.forEachIndexed { j, (k, v) ->
                sb.append("      ${jsonQuote(k)}: ${jsonQuote(v)}")
                sb.append(if (j < list.size - 1) ",\n" else "\n")
            }
            sb.append("    }").append(if (i < entries.size - 1) ",\n" else "\n")
        }
        sb.append("  }\n}\n")

        val out = file("src/main/res/raw/legal_notices.json")
        out.parentFile.mkdirs()
        out.writeText(sb.toString(), Charsets.UTF_8)
        logger.lifecycle("collectLegalNotices: ${entries.size} Bibliotheken mit NOTICE/LICENSE -> ${out.path}")
    }
}

val INCEPTION_YEAR = Regex("<inceptionYear>\\s*(\\d{4})")
val MANUAL_COPYRIGHT_YEARS = mapOf(
    "androidx.compose:compose-bom" to "2022",
)

tasks.register("collectCopyrightYears") {
    group = "legal"
    description = "Liest <inceptionYear> aus den POMs nach res/raw/copyright_years.json"
    notCompatibleWithConfigurationCache("Löst Runtime-Classpaths zur Ausführungszeit auf")
    doLast {
        val configs = LEGAL_CLASSPATHS.map { configurations.getByName(it) }
        val cacheRoot = configs.asSequence()
            .flatMap { it.resolvedConfiguration.resolvedArtifacts.asSequence() }
            .firstNotNullOfOrNull { generateSequence(it.file) { f -> f.parentFile }.firstOrNull { f -> f.name == "files-2.1" } }

        val years = sortedMapOf<String, String>()
        configs.flatMap { it.incoming.resolutionResult.allComponents }
            .mapNotNull { it.id as? ModuleComponentIdentifier }
            .forEach { id ->
                val coord = "${id.group}:${id.module}"
                if (coord in years || cacheRoot == null) return@forEach
                val versionDir = File(cacheRoot, "${id.group}/${id.module}/${id.version}")
                val pom = versionDir.takeIf { it.isDirectory }
                    ?.walkTopDown()?.firstOrNull { it.name == "${id.module}-${id.version}.pom" }
                    ?: return@forEach
                INCEPTION_YEAR.find(pom.readText())?.let { years[coord] = it.groupValues[1] }
            }
        MANUAL_COPYRIGHT_YEARS.forEach { (k, v) -> years.putIfAbsent(k, v) }

        val sb = StringBuilder().append("{\n  \"years\": {\n")
        years.entries.forEachIndexed { i, (coord, year) ->
            sb.append("    ${jsonQuote(coord)}: ${jsonQuote(year)}")
            sb.append(if (i < years.size - 1) ",\n" else "\n")
        }
        sb.append("  }\n}\n")

        val out = file("src/main/res/raw/copyright_years.json")
        out.parentFile.mkdirs()
        out.writeText(sb.toString(), Charsets.UTF_8)
        logger.lifecycle("collectCopyrightYears: ${years.size} Jahre -> ${out.path}")
    }
}

tasks.register("regenerateLegalData") {
    group = "legal"
    description = "Erzeugt AboutLibraries-Definitionen + legal_notices.json + copyright_years.json neu"
    dependsOn("exportLibraryDefinitionsRelease", "collectLegalNotices", "collectCopyrightYears")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.material.icons.extended)
    implementation(libs.haze)
    implementation(libs.sqlcipher.android)
    implementation(libs.androidx.sqlite.ktx)
    implementation(libs.androidx.biometric)
    implementation(libs.argon2kt)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.aboutlibraries.core)
    implementation(libs.androidx.profileinstaller)
    baselineProfile(project(":baselineprofile"))
    ksp(libs.room.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
