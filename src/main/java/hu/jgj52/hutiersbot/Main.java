package hu.jgj52.hutiersbot;

import hu.jgj52.hutiersbot.Buttons.*;
import hu.jgj52.hutiersbot.Commands.*;
import hu.jgj52.hutiersbot.Listeners.*;
import hu.jgj52.hutiersbot.Modals.*;
import hu.jgj52.hutiersbot.SelectMenus.*;
import hu.jgj52.hutiersbot.Types.*;
import hu.jgj52.hutiersbot.Utils.*;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Main {
    public static List<Command> commands = new ArrayList<>();
    public static List<Button> buttons = new ArrayList<>();
    public static List<SelectMenu> selectmenus = new ArrayList<>();
    public static List<Modal> modals = new ArrayList<>();
    public static PostgreSQL postgres;
    public static Guild guild;
    public static Long testCooldown;

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        testCooldown = Long.parseLong(dotenv.get("TEST_COOLDOWN")) * 1000;

        try {
            postgres = new PostgreSQL(dotenv.get("POSTGRES_HOST"), Integer.parseInt(dotenv.get("POSTGRES_PORT")), dotenv.get("POSTGRES_DATABASE"), dotenv.get("POSTGRES_USER"), dotenv.get("POSTGRES_PASSWORD"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        JDABuilder builder = JDABuilder.createLight(dotenv.get("TOKEN"), Set.of(GatewayIntent.values()));
        builder.addEventListeners(new CommandListener());
        builder.addEventListeners(new ButtonListener());
        builder.addEventListeners(new SelectMenuListener());
        builder.addEventListeners(new ModalListener());
        builder.addEventListeners(new ReadyListener());
        builder.build();

        buttons.add(new StopNameUpdatingButton());

        commands.add(new UpdateNamesCommand());
        commands.add(new RequestTestCommand());

        selectmenus.add(new RequestTestSelectMenu());

        //modals.add();
    }
}