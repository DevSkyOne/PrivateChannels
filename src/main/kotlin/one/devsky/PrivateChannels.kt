package one.devsky

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.utils.cache.CacheFlag
import one.devsky.extensions.getLogger
import one.devsky.manager.Environment
import one.devsky.manager.RegisterManager.registerAll
import one.devsky.manager.RegisterManager.registerCommands
import java.util.*

class PrivateChannels {

    companion object {
        lateinit var instance: PrivateChannels
    }

    private val jda: JDA
    val properties = Properties()

    init {
        instance = this

        jda = JDABuilder.createDefault(Environment.getEnv("BOT_TOKEN"))
            .disableCache(CacheFlag.SCHEDULED_EVENTS)
            .enableCache(CacheFlag.VOICE_STATE)
            .registerAll()
            .build()
            .awaitReady()
            .registerCommands()

        getLogger().info("Bot is ready! ${jda.selfUser.name} - ${jda.selfUser.id} on ${jda.guilds.size} guilds")
    }
}