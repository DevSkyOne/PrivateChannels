package one.devsky.listeners

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import one.devsky.annotations.SlashCommand
import one.devsky.interfaces.HasSubcommands
import one.devsky.manager.Environment
import one.devsky.manager.TempStorage

@SlashCommand("editprivatechannel", "Edits a private channel", true)
class EditPrivateChannelCommand : ListenerAdapter(), HasSubcommands {

    override fun getSubCommands(): List<SubcommandData> {
        return listOf(
            SubcommandData("userlimit", "Editiert die Userlimit eines privaten Channels")
                .addOptions(
                    OptionData(OptionType.INTEGER, "userlimit", "Die Userlimit des Channels", true)
                ),
            SubcommandData("name", "Editiert den Namen eines privaten Channels")
                .addOptions(
                    OptionData(OptionType.STRING, "name", "Der Name des Channels", true)
                ),
            SubcommandData("sichtbarkeit", "Ändert die Sichtbarkeit eines privaten Channels")
        )
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) = with(event) {
        if (name != "editprivatechannel") return@with

        if (member?.voiceState?.channel == null) {
            reply("Du bist in keinem Channel").setEphemeral(true).queue()
            return
        }

        if (!TempStorage.getList("tempchannels").contains(member!!.voiceState!!.channel!!.id)) {
            reply("Du bist in keinem privaten Channel").setEphemeral(true).queue()
            return
        }

        when (subcommandName) {
            "userlimit" -> onUserLimit(event)
            "name" -> onName(event)
            "sichtbarkeit" -> onVisibility(event)
        }
    }

    private fun onUserLimit(event: SlashCommandInteractionEvent) = with(event) {
        val userLimit = getOption("userlimit")?.asInt ?: -1
        if (userLimit <= 1) return@with reply("Das Userlimit muss größer als 1 sein").setEphemeral(true).queue()

        if (userLimit > 99) return@with reply("Das Userlimit darf nicht größer als 99 sein").setEphemeral(true).queue()

        val voiceChannel =
            member?.voiceState?.channel as VoiceChannel? ?: return@with reply("Du bist in keinem Channel").setEphemeral(
                true
            ).queue()
        if (voiceChannel.memberPermissionOverrides.none { it.member!!.id == user.id } && !member!!.hasPermission(
                Permission.MANAGE_CHANNEL
            )) return@with reply("Du hast keine Berechtigungen auf diesen Kanal.").setEphemeral(true).queue()
        voiceChannel.manager.setUserLimit(userLimit).queue()
        reply("Userlimit erfolgreich geändert").setEphemeral(true).queue()
    }

    private fun onName(event: SlashCommandInteractionEvent) = with(event) {
        val name = getOption("name")?.asString ?: return@with reply("Kein Name angegeben").setEphemeral(true).queue()

        val voiceChannel =
            member?.voiceState?.channel as VoiceChannel? ?: return@with reply("Du bist in keinem Channel").setEphemeral(
                true
            ).queue()
        if (voiceChannel.memberPermissionOverrides.none { it.member!!.id == user.id } && !member!!.hasPermission(
                Permission.MANAGE_CHANNEL
            )) return@with reply("Du hast keine Berechtigungen auf diesen Kanal.").setEphemeral(true).queue()
        voiceChannel.manager.setName("${Environment.icons.random()}-" + name).queue()
        reply("Name erfolgreich geändert").setEphemeral(true).queue()
    }

    private fun onVisibility(event: SlashCommandInteractionEvent) = with(event) {
        val voiceChannel =
            member?.voiceState?.channel as VoiceChannel? ?: return@with reply("Du bist in keinem Channel").setEphemeral(
                true
            ).queue()
        if (voiceChannel.memberPermissionOverrides.none { it.member!!.id == user.id } && !member!!.hasPermission(
                Permission.MANAGE_CHANNEL
            )) return@with reply("Du hast keine Berechtigungen auf diesen Kanal.").setEphemeral(true).queue()
        val currentPermission = voiceChannel.getPermissionOverride(voiceChannel.guild.publicRole)

        var sichtbarkeit = "sichtbar"

        if (currentPermission == null) {
            voiceChannel.upsertPermissionOverride(voiceChannel.guild.publicRole).deny(Permission.VIEW_CHANNEL).queue()
            sichtbarkeit = "unsichtbar"
        } else if (currentPermission.allowed.contains(Permission.VIEW_CHANNEL)) {
            voiceChannel.upsertPermissionOverride(voiceChannel.guild.publicRole).deny(Permission.VIEW_CHANNEL).queue()
            sichtbarkeit = "unsichtbar"
        } else if (currentPermission.denied.contains(Permission.VIEW_CHANNEL)) {
            voiceChannel.upsertPermissionOverride(voiceChannel.guild.publicRole).grant(Permission.VIEW_CHANNEL).queue()
        }

        reply("Der Channel ist nun $sichtbarkeit").setEphemeral(true).queue()
    }
}