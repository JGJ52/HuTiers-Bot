package hu.jgj52.hutiersbot;

import hu.jgj52.hutiersbot.Commands.CicaCommand;
import hu.jgj52.hutiersbot.Listeners.CommandListener;
import hu.jgj52.hutiersbot.Types.Command;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Main {
    public static List<Command> commands = new ArrayList<>();

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        JDABuilder builder = JDABuilder.createLight(dotenv.get("TOKEN"), Set.of(GatewayIntent.values()));
        builder.addEventListeners(new CommandListener());
        JDA jda = builder.build();

        commands.add(new CicaCommand());

        CommandListUpdateAction jdaCommands = jda.updateCommands();
        List<SlashCommandData> cmds = new ArrayList<>();
        for (Command cmd : commands) cmds.add(cmd.command());
        jdaCommands.addCommands(cmds);
        jdaCommands.queue();
    }
}