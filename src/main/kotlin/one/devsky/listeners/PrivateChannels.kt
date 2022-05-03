package one.devsky.listeners

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import one.devsky.annotations.SlashCommand
import one.devsky.interfaces.HasOptions


@SlashCommand("setupprivatechannels", "Erstelle temporäre Audiochannel", guilds = ["828274529070612539"])
class PrivateChannels : ListenerAdapter(), HasOptions {

    override fun getOptions(): List<OptionData> {
        return listOf(
            OptionData(OptionType.CHANNEL, "channel", "Wähle einen Voicechannel", true).setChannelTypes(ChannelType.VOICE)
        )
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) = with(event) {
        if(name != "setupprivatechannels") return@with

        val audioChannel = getOption("channel")?.asAudioChannel ?: return@with reply("Kein Voicechannel gewählt").setEphemeral(true).queue()


        reply("Der JoinKanal für die temporären Audiochannels wurde auf ${audioChannel.asMention} gesetzt.").setEphemeral(true).queue()
    }
}