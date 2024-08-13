package one.devsky.manager

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import one.devsky.annotations.SlashCommand
import one.devsky.extensions.getLogger
import one.devsky.interfaces.HasSubcommandGroups
import one.devsky.interfaces.HasOptions
import one.devsky.interfaces.HasSubcommands
import org.reflections8.Reflections
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

object RegisterManager {

    private var loadedClasses = mapOf<String, Any>()

    @OptIn(ExperimentalTime::class)
    fun JDABuilder.registerAll() : JDABuilder {
        val reflections = Reflections("one.devsky")

        // Registering both ListenerAdapters and EventListeners
        val listenerTime = measureTime {
            for (clazz in (reflections.getSubTypesOf(ListenerAdapter::class.java) + reflections.getSubTypesOf(EventListener::class.java)).distinct()) {
                if (clazz.simpleName == "ListenerAdapter") continue

                val constructor = clazz.getDeclaredConstructor()
                constructor.trySetAccessible()

                val listener = constructor.newInstance()
                loadedClasses += clazz.simpleName to listener
                addEventListeners(listener)
                getLogger().info("Registered listener: ${listener.javaClass.simpleName}")
            }
        }
        getLogger().info("Registered listeners in $listenerTime")

        return this
    }

    @OptIn(ExperimentalTime::class)
    fun JDA.registerCommands(): JDA {
        val reflections = Reflections("one.devsky")

        // Registering commands
        val commandsTime = measureTime {
            for (clazz in reflections.getTypesAnnotatedWith(SlashCommand::class.java)) {
                val annotation = clazz.getAnnotation(SlashCommand::class.java)
                val data = Commands.slash(annotation.name, annotation.description)

                if (clazz.simpleName !in loadedClasses) {
                    val constructor = clazz.getDeclaredConstructor()
                    constructor.trySetAccessible()

                    val command = constructor.newInstance()
                    loadedClasses += clazz.simpleName to command
                    getLogger().info("Registered command class: ${command.javaClass.simpleName}")
                }

                val command = loadedClasses[clazz.simpleName]

                if (command is HasOptions) {
                    data.addOptions(command.getOptions())
                }

                if (command is HasSubcommands) {
                    data.addSubcommands(command.getSubCommands())
                }

                if (command is HasSubcommandGroups) {
                    data.addSubcommandGroups(command.getChoices())
                }

                if(annotation.globalCommand) {
                    upsertCommand(data).queue()
                    getLogger().info("Registered global command: ${annotation.name}")
                } else {
                    for (guildID in annotation.guilds) {
                        getGuildById(guildID)?.let { guild ->
                            guild.upsertCommand(data).queue()
                            getLogger().info("Registered command: ${annotation.name} in guild: ${guild.name}")
                        }
                    }
                }
            }
        }
        getLogger().info("Registered commands in $commandsTime")

        return this
    }
}