package hu.jgj52.hutiersbot;

import hu.jgj52.hutiersbot.Buttons.CicaButton;
import hu.jgj52.hutiersbot.Commands.CicaSlashCommand;
import hu.jgj52.hutiersbot.Listeners.ButtonListener;
import hu.jgj52.hutiersbot.Listeners.CommandListener;
import hu.jgj52.hutiersbot.Types.Button;
import hu.jgj52.hutiersbot.Types.SlashCommand;
import hu.jgj52.hutiersbot.Utils.PostgreSQL;
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
    public static List<SlashCommand> commands = new ArrayList<>();
    public static List<Button> buttons = new ArrayList<>();
    public static PostgreSQL postgres;

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        try {
            postgres = new PostgreSQL(dotenv.get("POSTGRES_HOST"), Integer.parseInt(dotenv.get("POSTGRES_PORT")), dotenv.get("POSTGRES_DATABASE"), dotenv.get("POSTGRES_USER"), dotenv.get("POSTGRES_PASSWORD"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        JDABuilder builder = JDABuilder.createLight(dotenv.get("TOKEN"), Set.of(GatewayIntent.values()));
        builder.addEventListeners(new CommandListener());
        builder.addEventListeners(new ButtonListener());
        JDA jda = builder.build();

        buttons.add(new CicaButton());

        commands.add(new CicaSlashCommand());

        CommandListUpdateAction jdaCommands = jda.updateCommands();
        List<SlashCommandData> cmds = new ArrayList<>();
        for (SlashCommand cmd : commands) cmds.add(cmd.command());
        jdaCommands.addCommands(cmds);
        jdaCommands.queue();
    }
}