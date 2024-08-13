package one.devsky.manager

import io.github.cdimascio.dotenv.dotenv

object Environment {

    private val env = System.getenv()
    private val dotEnv = dotenv {
        ignoreIfMissing = true
    }

    val icons = setOf(
        "\uD83D\uDFE0",
        "\uD83D\uDFE1",
        "\uD83D\uDFE2",
        "\uD83D\uDFE3",
        "\uD83D\uDFE4",
        "\uD83D\uDFE5",
        "\uD83D\uDFE7",
        "\uD83D\uDFE8",
        "\uD83D\uDFE9",
        "\uD83D\uDFE6",
        "\uD83D\uDFEA",
        "\uD83D\uDFEB"
    )

    /**
     * Retrieves the value of the environment variable with the specified key.
     *
     * @param key The key of the environment variable.
     * @return The value of the environment variable, or null if the variable is not found.
     */
    fun getEnv(key: String): String? {
        return dotEnv[key] ?: env[key]
    }

}