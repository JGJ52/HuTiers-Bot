package hu.jgj52.hutiersbot.Commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hu.jgj52.hutiersbot.Buttons.StopNameUpdatingButton;
import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Button;
import hu.jgj52.hutiersbot.Types.Command;
import hu.jgj52.hutiersbot.Utils.PostgreSQL;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class UpdateNamesCommand extends Command {
    private Long last = 0L;
    private boolean running = false;
    public static boolean stop = false;

    @Override
    public String getName() {
        return "updatenames";
    }

    @Override
    public String getDescription() {
        return "Update the names on the tierlist";
    }

    @Override
    public void addOptions(SlashCommandData command) {
        command.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (last > System.currentTimeMillis()) {
            event.reply("Csak <t:" + last / 1000 + ":R> múlva használhatod ezt a parancsot!").setEphemeral(true).queue();
            return;
        }

        if (running) {
            event.reply("Már folyamatban van egy névfrissítés!").setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue();

        CompletableFuture.runAsync(() -> {
            running = true;
            try {
                HttpClient client = HttpClient.newHttpClient();
                Gson gson = new Gson();
                PostgreSQL.QueryResult result = Main.postgres.from("players").order("id").execute().get();
                int updated = 0;

                for (int i = 0; i < result.data.size(); i++) {
                    if (stop) break;

                    Map<String, Object> player = result.data.get(i);
                    String uuid = player.get("uuid").toString().replaceAll("-", "");

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid))
                            .GET()
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 429) {
                        event.getHook().editOriginal("A Mojang ratelimitelt, szóval most várok 10 percet.\nEddig frissítve: " + updated).queue();
                        Thread.sleep(10 * 60 * 1000);
                        i--;
                        continue;
                    }

                    if (response.statusCode() != 200) continue;

                    JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                    if (json.has("name")) {
                        if (!Objects.equals(json.get("name").getAsString(), player.get("name").toString())) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("name", json.get("name").getAsString());
                            Main.postgres.from("players").eq("id", player.get("id")).update(data);
                            updated++;
                        }
                        Button button = new StopNameUpdatingButton();
                        event.getHook().editOriginal(
                                player.get("uuid").toString() + "\n" +
                                        player.get("name").toString().replaceAll("_", "\\\\_") + " -> " + json.get("name").getAsString().replaceAll("_", "\\\\_") + "\n" +
                                        (i + 1) + "/" + result.data.size() + "\n" +
                                        "Frissítve: " + updated
                        ).setComponents(ActionRow.of(button.button())).queue();
                    }

                    Thread.sleep(100);
                }

                event.getHook().editOriginal("Játékosnevek frissítve!\nÖsszesen ennyi név lett frissítve: " + updated).setComponents().queue();
                last = System.currentTimeMillis() + 1000 * 60 * 15;
                running = false;

            } catch (Exception e) {
                e.printStackTrace();
                event.getHook().sendMessage("Hiba: " + e.getMessage()).queue();
            }
        });
    }

}
