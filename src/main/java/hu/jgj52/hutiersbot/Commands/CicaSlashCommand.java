package hu.jgj52.hutiersbot.Commands;

import hu.jgj52.hutiersbot.Buttons.CicaButton;
import hu.jgj52.hutiersbot.Types.Button;
import hu.jgj52.hutiersbot.Types.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.awt.Color;

public class CicaSlashCommand extends SlashCommand {
    @Override
    public String getName() {
        return "cica";
    }

    @Override
    public String getDescription() {
        return "nagyon cica";
    }

    @Override
    public void addOptions(SlashCommandData command) {
        command.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("TIT.le");
        embed.setDescription("nagyon icca");
        embed.setColor(Color.CYAN);
        embed.addField("name", "nem", false);
        embed.addField("eman", "men", true);
        embed.setFooter("fúter");

        Button button = new CicaButton();

        event.getChannel().sendMessage("").addEmbeds(embed.build()).setComponents(ActionRow.of(button.button())).queue();

        event.reply("sent").setEphemeral(true).queue();
    }
}
