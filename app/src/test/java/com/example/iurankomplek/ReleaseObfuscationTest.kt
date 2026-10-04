package com.example.iurankomplek

import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

/**
 * Guards Gson model classes against R8 renaming them in release builds.
 *
 * These models declare no `@SerializedName`, so Gson maps JSON keys by reflecting
 * on field names. The release build sets `minifyEnabled true`, so if a keep rule
 * stops matching their package, every field name is obfuscated and deserialization
 * silently yields null/zero values -- a defect that unit tests cannot observe,
 * because they always run against unminified classes.
 *
 * Regression: the keep rule named `com.example.iurankomplek.data.model.**`, a
 * package that does not exist, so all of `model/` and `data/api/models/` were
 * obfuscated in release.
 */
class ReleaseObfuscationTest {

    private val moduleDir: File = findModuleDir()

    private val sourceRoot = File(moduleDir, "src/main/java")
    private val proguardFile = File(moduleDir, "proguard-rules.pro")

    /** Packages whose classes are (de)serialized by Gson via reflection. */
    private val gsonModelPackages = listOf(
        "com.example.iurankomplek.model",
        "com.example.iurankomplek.data.api.models",
    )

    @Test
    fun `every Gson model package is covered by a keep rule`() {
        val keepRules = keptPackages()

        val uncovered = gsonModelPackages.filter { pkg ->
            keepRules.none { pkg == it || pkg.startsWith("$it.") }
        }

        if (uncovered.isNotEmpty()) {
            fail(
                "Gson model packages have no matching -keep rule in proguard-rules.pro: " +
                    "$uncovered. Release builds would obfuscate their field names and " +
                    "silently deserialize all values as null/zero."
            )
        }
    }

    @Test
    fun `keep rules do not reference project packages that do not exist`() {
        val phantom = keptPackages()
            .filter { it.startsWith("com.example.iurankomplek") }
            .distinct()
            .filter { pkg -> !File(sourceRoot, pkg.replace('.', '/')).exists() }

        if (phantom.isNotEmpty()) {
            fail(
                "proguard-rules.pro keeps project packages that do not exist in " +
                    "src/main/java: $phantom. Such rules silently match nothing, which " +
                    "is how the Gson model obfuscation regression shipped."
            )
        }
    }

    @Test
    fun `Gson model classes do not rely on obfuscated field names`() {
        val withoutAnnotations = gsonModelPackages.flatMap { pkg ->
            val dir = File(sourceRoot, pkg.replace('.', '/'))
            if (!dir.exists()) return@flatMap emptyList()
            dir.listFiles { f -> f.extension == "kt" }.orEmpty().toList()
        }.filter { file ->
            !file.readText().contains("@SerializedName")
        }.map { it.name }

        // Not a failure by itself: the keep rules are what make it safe. This test
        // exists so that removing the keep rules becomes a deliberate decision
        // rather than an accident.
        assertTrue(
            "Expected unannotated Gson models (keep rules are what protect these)",
            withoutAnnotations.isNotEmpty()
        )
    }

    private fun keptPackages(): List<String> {
        assertTrue("proguard-rules.pro not found at $proguardFile", proguardFile.exists())
        return proguardFile.readLines()
            .map { it.trim() }
            .filter { it.startsWith("-keep") && !it.startsWith("-keepattributes") }
            .mapNotNull { line ->
                Regex("-keep(?:class)?\\s+(?:class\\s+)?([A-Za-z0-9_.$*]+)")
                    .find(line)?.groupValues?.get(1)
                    ?.removeSuffix(".**")?.removeSuffix(".*")
                    ?.takeIf { it.isNotBlank() }
            }
    }

    private fun findModuleDir(): File {
        var dir: File? = File("").absoluteFile
        while (dir != null) {
            if (File(dir, "proguard-rules.pro").exists() &&
                File(dir, "src/main/java").exists()
            ) {
                return dir
            }
            dir = dir.parentFile
        }
        throw IllegalStateException("Could not locate the app module directory")
    }
}