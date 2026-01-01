package hu.jgj52.hutiersbot.Types;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public abstract class Command {
    public abstract String getName();
    public abstract String getDescription();
    protected void addOptions(SlashCommandData command) {}

    public final SlashCommandData command() {
        SlashCommandData command = Commands.slash(getName(), getDescription());
        addOptions(command);
        return command;
    }

    public abstract void execute(SlashCommandInteractionEvent event);
}
