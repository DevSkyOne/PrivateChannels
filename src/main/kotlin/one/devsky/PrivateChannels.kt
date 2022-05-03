package one.devsky

import de.moltenKt.core.extension.logging.getLogger
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import one.devsky.manager.RegisterManager.registerAll
import one.devsky.manager.RegisterManager.registerCommands

class PrivateChannels {

    companion object {
        lateinit var instance: PrivateChannels
    }

    private val jda: JDA

    init {
        instance = this

        jda = JDABuilder.createDefault(System.getenv("BOT_TOKEN"))
            .registerAll()
            .build()
            .awaitReady()
            .registerCommands()

        getLogger(PrivateChannels::class).info("Bot is ready! ${jda.selfUser.name} - ${jda.selfUser.id} on ${jda.guilds.size} guilds")
    }

}