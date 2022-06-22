package one.devsky.listeners

import de.moltenKt.core.extension.logging.getItsLogger
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.AudioChannel
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import one.devsky.PrivateChannels
import one.devsky.annotations.SlashCommand
import one.devsky.extensions.addToList
import one.devsky.extensions.getList
import one.devsky.extensions.removeFromList
import one.devsky.extensions.set
import one.devsky.interfaces.HasOptions
import java.util.logging.Level


@SlashCommand("setupprivatechannels", "Erstelle temporäre Audiochannel", true)
class PrivateChannelsListener : ListenerAdapter(), HasOptions {

    override fun getOptions(): List<OptionData> {
        return listOf(
            OptionData(OptionType.CHANNEL, "channel", "Wähle einen Voicechannel", true).setChannelTypes(ChannelType.VOICE)
        )
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) = with(event) {
        if(name != "setupprivatechannels") return@with

        val audioChannel = getOption("channel")?.asAudioChannel ?: return@with reply("Kein Voicechannel gewählt").setEphemeral(true).queue()
        PrivateChannels.instance.properties.set("joinChannel", audioChannel.id)
        reply("Der JoinKanal für die temporären Audiochannels wurde auf ${audioChannel.asMention} gesetzt.").setEphemeral(true).queue()
    }

    private val icons = setOf("\uD83D\uDFE0", "\uD83D\uDFE1", "\uD83D\uDFE2", "\uD83D\uDFE3", "\uD83D\uDFE4", "\uD83D\uDFE5", "\uD83D\uDFE7", "\uD83D\uDFE8",
    "\uD83D\uDFE9", "\uD83D\uDFE6", "\uD83D\uDFEA", "\uD83D\uDFEB")

    override fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) = with(event) {
        onJoinChannel(guild, channelJoined, member)
    }

    override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) = with(event) {
        onLeaveChannel(channelLeft)
    }

    override fun onGuildVoiceMove(event: GuildVoiceMoveEvent) = with(event) {
        onLeaveChannel(channelLeft)
        onJoinChannel(guild, channelJoined, member)
    }

    private fun onLeaveChannel(channelLeft: AudioChannel) {
        if (!PrivateChannels.instance.properties.getList("channels").contains(channelLeft.id)) return

        if(channelLeft.members.size == 0) {
            channelLeft.delete().queue()
            PrivateChannels.instance.properties.removeFromList("channels", channelLeft.id)
        } else {
            println("${channelLeft.members.size} members left in ${channelLeft.name}")
        }
    }

    private fun onJoinChannel(guild: Guild, channelJoined: AudioChannel, member: Member) {
        if(PrivateChannels.instance.properties["joinChannel"] != channelJoined.id) return

        val category = PrivateChannels.instance.properties["category"]?.toString()?.let { guild.getCategoryById(it) } ?: guild.categories.find { it.voiceChannels.contains(channelJoined) } ?: return getItsLogger().log(
            Level.WARNING, "Es wurde keine passende Kategorie für einen Voicechannel gefunden")

        val talkId = PrivateChannels.instance.properties.getProperty("talkId", "1").toInt()
        PrivateChannels.instance.properties.set("talkId", (talkId + 1).toString())

        category.createVoiceChannel("${icons.random().apply { println("Char is: $this") }}-Talk $talkId")
            .addMemberPermissionOverride(member.idLong, Permission.PRIORITY_SPEAKER.rawValue, 0L)
            .queue { voice ->
                PrivateChannels.instance.properties.addToList("channels", voice.id)
                guild.moveVoiceMember(member, voice).queue()
            }
    }

}