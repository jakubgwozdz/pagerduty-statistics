package pdstats

import java.nio.file.Files
import java.nio.file.Paths
import java.util.Properties

// this is a read-write variable, but any changes after first access to Environment object will be discarded
var localEnvFile = "local/.env"

/**
 * Handy accessor to environment properties with possibility to read from local/.env file
 *
 * Accessing via Environment["key"] returns nullable String
 * Accessing via Environment.mandatory["key"] returns non-null String or exception when not found
 */
object Environment {

    private val local by lazy {
        val localPath = Paths.get(localEnvFile)
        val localProperties = Properties()
        if (Files.isReadable(localPath)) {
            Files.newBufferedReader(localPath).use { localProperties.load(it) }
        }
        localProperties
    }

    operator fun get(key: String): String? = local.getProperty(key) ?: System.getenv(key)
    operator fun set(key: String, value: String) { local.setProperty(key, value) }

    val mandatory = object : ParamMap {
        override operator fun get(key: String) = this@Environment[key] ?: error("Parameter `$key` not set")
        override operator fun set(key: String, value: String) { local.setProperty(key, value) }
    }

    // we need this only for `mandatory` property to not be `Any` and actually expose the member functions
    interface ParamMap {
        operator fun get(key: String): String
        operator fun set(key: String, value: String)
    }
}
