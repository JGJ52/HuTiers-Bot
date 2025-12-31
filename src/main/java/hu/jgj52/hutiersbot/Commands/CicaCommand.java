package hu.jgj52.hutiersbot.Commands;

import hu.jgj52.hutiersbot.Types.Command;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class CicaCommand extends Command {
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
        command.addOption(OptionType.BOOLEAN, "cica", "is it cica", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.reply("nagoyncica.").queue();
    }
}
