package one.devsky.boilerplates

import de.moltenKt.core.extension.logging.getLogger
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import one.devsky.boilerplates.manager.RegisterManager.registerAll
import one.devsky.boilerplates.manager.RegisterManager.registerCommands

class JDA5Boilerplate {

    companion object {
        lateinit var instance: JDA5Boilerplate
    }

    private val jda: JDA

    init {
        instance = this

        jda = JDABuilder.createDefault(System.getenv("BOT_TOKEN"))
            .registerAll()
            .build()
            .awaitReady()
            .registerCommands()

        getLogger(JDA5Boilerplate::class).info("Bot is ready! ${jda.selfUser.name} - ${jda.selfUser.id} on ${jda.guilds.size} guilds")
    }

}