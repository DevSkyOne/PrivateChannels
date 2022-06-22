package one.devsky.listeners

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import one.devsky.annotations.SlashCommand
import one.devsky.interfaces.HasSubcommands

@SlashCommand("editprivatechannel", "Edits a private channel", true)
class EditPrivateChannelCommand : ListenerAdapter(), HasSubcommands {

    override fun getSubCommands(): List<SubcommandData> {
        return listOf(
            SubcommandData("userlimit", "Editiert die Userlimit eines privaten Channels")
                .addOptions(
                    OptionData(OptionType.INTEGER, "userlimit", "Die Userlimit des Channels", true)
                ),
        )
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) = with(event) {
        if(name != "editprivatechannel") return@with

        when(subcommandName) {
            "userlimit" -> onUserLimit(event)
        }
    }

    private fun onUserLimit(event: SlashCommandInteractionEvent) = with(event) {
       val userLimit = getOption("userlimit")?.asInt ?: -1
        if(userLimit <= 1) return@with reply("Das Userlimit muss größer als 1 sein").setEphemeral(true).queue()

        val voiceChannel = member?.voiceState?.channel as VoiceChannel? ?: return@with reply("Du bist in keinem Channel").setEphemeral(true).queue()
        if(voiceChannel.memberPermissionOverrides.find { it.member!!.id == user.id } == null && !member!!.hasPermission(Permission.MANAGE_CHANNEL)) return@with reply("Du hast keine Berechtigungen auf diesen Kanal.").setEphemeral(true).queue()
        voiceChannel.manager.setUserLimit(userLimit).queue()
        reply("Userlimit erfolgreich geändert").setEphemeral(true).queue()
    }
}