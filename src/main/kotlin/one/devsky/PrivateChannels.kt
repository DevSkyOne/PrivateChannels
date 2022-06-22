package one.devsky

import de.moltenKt.core.extension.logging.getLogger
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import one.devsky.extensions.saveProperties
import one.devsky.manager.RegisterManager.registerAll
import one.devsky.manager.RegisterManager.registerCommands
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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

        val inputStream = FileInputStream(propertiesFile)
        properties.load(inputStream)

        jda = JDABuilder.createDefault(properties.getProperty("BOT_TOKEN", System.getenv("BOT_TOKEN")))
            .registerAll()
            .build()
            .awaitReady()
            .registerCommands()

        getLogger(PrivateChannels::class).info("Bot is ready! ${jda.selfUser.name} - ${jda.selfUser.id} on ${jda.guilds.size} guilds")
    }
}