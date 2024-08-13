package one.devsky.listeners

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import one.devsky.annotations.SlashCommand
import one.devsky.extensions.getLogger
import one.devsky.interfaces.HasOptions
import one.devsky.manager.Environment
import one.devsky.manager.TempStorage


@SlashCommand("setupprivatechannels", "Erstelle temporäre Audiochannel", true)
class PrivateChannelsListener : ListenerAdapter(), HasOptions {

    override fun getOptions(): List<OptionData> {
        return listOf(
            OptionData(
                OptionType.CHANNEL,
                "channel",
                "Wähle einen Voicechannel",
                true
            ).setChannelTypes(ChannelType.VOICE),
            OptionData(
                OptionType.CHANNEL,
                "category",
                "Wähle eine Kategorie (optional)",
                false
            ).setChannelTypes(ChannelType.CATEGORY)
        )
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) = with(event) {
        if (name != "setupprivatechannels") return@with
        if (guild == null) return@with

        val audioChannel =
            getOption("channel")?.asChannel ?: return@with reply("Kein Channel gewählt").setEphemeral(true).queue()
        if (audioChannel !is AudioChannel) return@with reply("Der gewählte Channel ist kein Voicechannel").setEphemeral(
            true
        ).queue()

        val category = getOption("category")?.asChannel ?: audioChannel.asAudioChannel().parentCategory

        TempStorage.saveTempFile("tempchannels.${guild!!.id}.channel", (audioChannel as AudioChannel).id)
        category?.id?.let { TempStorage.saveTempFile("tempchannels.${guild!!.id}.category", it) }
        reply("Der JoinKanal für die temporären Audiochannels wurde auf ${(audioChannel as AudioChannel).asMention} gesetzt.").setEphemeral(
            true
        ).queue()
    }

    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) = with(event) {
        val joinChannel = channelJoined
        val leaveChannel = channelLeft

        if (joinChannel == null && leaveChannel == null) return@with
        if (joinChannel != null && leaveChannel != null) run {
            onLeaveChannel(leaveChannel)
            onJoinChannel(guild, joinChannel, member)
            return@with
        }
        if (joinChannel != null) return@with onJoinChannel(guild, joinChannel, member)
        if (leaveChannel != null) return@with onLeaveChannel(leaveChannel)

    }

    private fun onLeaveChannel(channelLeft: AudioChannel) {
        if (!TempStorage.getList("tempchannels").contains(channelLeft.id)) return

        if (channelLeft.members.isNotEmpty()) return

        channelLeft.delete().queue()
        TempStorage.removeFromList("tempchannels", channelLeft.id)
    }

    private fun onJoinChannel(guild: Guild, channelJoined: AudioChannel, member: Member) {
        if (TempStorage.readTempFileAsString("tempchannels.${guild.id}.channel") != channelJoined.id) return

        val category = TempStorage.readTempFileAsStringOrNull("tempchannels.${guild.id}.category")?.let { guild.getCategoryById(it) }
            ?: guild.categories.find { it.voiceChannels.contains(channelJoined) }
            ?: return getLogger().warn("Es wurde keine passende Kategorie für einen Voicechannel gefunden")

        val talkId = TempStorage.readTempFileAsStringOrNull("talkId")?.toIntOrNull() ?: 0
        TempStorage.saveTempFile("talkId", (talkId + 1).toString())

        if (!guild.selfMember.canSync(category)) {
            member.user.openPrivateChannel().queue { it.sendMessage("Es ist ein Fehler beim Erstellen eines privaten Channels aufgetreten. `Fehlende Berechtigungen`").queue() }
            return
        }

        category.createVoiceChannel("${Environment.icons.random()}-Talk $talkId")
            .addMemberPermissionOverride(member.idLong, Permission.PRIORITY_SPEAKER.rawValue, 0L)
            .queue (
                { voice ->
                    TempStorage.addToList("tempchannels", voice.id)
                    guild.moveVoiceMember(member, voice).queue()
                },
                { sendErrorMessageToUser(member, it) }
            )
    }


    private fun sendErrorMessageToUser(member: Member, throwable: Throwable) {
        getLogger().error("Fehler beim Erstellen eines privaten Channels", throwable)
        member.user.openPrivateChannel().queue { it.sendMessage("Es ist ein Fehler beim Erstellen eines privaten Channels aufgetreten.\n \n " +
                "```kt \n" +
                throwable.message +
                "\n ```").queue() }

    }
}