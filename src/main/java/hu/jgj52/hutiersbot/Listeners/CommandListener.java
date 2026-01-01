package hu.jgj52.hutiersbot.Listeners;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.SlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Objects;

public class CommandListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();
        for (SlashCommand cmd : Main.commands) {
            if (Objects.equals(cmd.getName(), command)) {
                cmd.execute(event);
                break;
            }
        }
    }
}
