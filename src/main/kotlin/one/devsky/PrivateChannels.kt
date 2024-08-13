package one.devsky

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import one.devsky.extensions.getLogger
import one.devsky.extensions.saveProperties
import one.devsky.manager.Environment
import one.devsky.manager.RegisterManager.registerAll
import one.devsky.manager.RegisterManager.registerCommands
import java.io.File
import java.util.*

class PrivateChannels {

    companion object {
        lateinit var instance: PrivateChannels
    }

    private val jda: JDA
    val properties = Properties()

    init {
        instance = this

        val propertiesFile = "data.properties"
        val file = File(propertiesFile)

        if (!file.exists()) {
            saveProperties()
        }

        jda = JDABuilder.createDefault(Environment.getEnv("BOT_TOKEN"))
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .disableCache(CacheFlag.SCHEDULED_EVENTS)
            .enableCache(CacheFlag.VOICE_STATE)
            .registerAll()
            .build()
            .awaitReady()
            .registerCommands()

        getLogger().info("Bot is ready! ${jda.selfUser.name} - ${jda.selfUser.id} on ${jda.guilds.size} guilds")
    }
}