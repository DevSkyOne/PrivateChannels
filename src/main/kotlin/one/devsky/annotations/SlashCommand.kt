package one.devsky.annotations

annotation class SlashCommand(
    val name: String,
    val description: String,
    val globalCommand: Boolean = false,
    val guilds: Array<String> = []
)
