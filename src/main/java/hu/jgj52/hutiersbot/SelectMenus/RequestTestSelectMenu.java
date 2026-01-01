package hu.jgj52.hutiersbot.SelectMenus;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Gamemode;
import hu.jgj52.hutiersbot.Types.SelectMenu;
import hu.jgj52.hutiersbot.Utils.PostgreSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import java.util.*;

public class RequestTestSelectMenu extends SelectMenu {
    @Override
    public String getCustomId() {
        return "requesthightest";
    }

    @Override
    public String getPlaceholder() {
        return "Válassz játékmódot";
    }

    @Override
    public Map<String, Map<Emoji, String>> getOptions() {
        try {
            PostgreSQL.QueryResult result = Main.postgres.from("gamemodes").order("id").execute().get();
            Map<String, Map<Emoji, String>> data = new LinkedHashMap<>();
            for (Map<String, Object> row : result.data) {
                Gamemode gamemode = new Gamemode(row);
                Map<Emoji, String> emojiStringMap = new HashMap<>();
                emojiStringMap.put(gamemode.getEmoji(), gamemode.getName());
                data.put(String.valueOf(gamemode.getId()), emojiStringMap);
            }
            return data;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execute(StringSelectInteractionEvent event) {
        Gson gson = new Gson();
        try {
            PostgreSQL.QueryResult result = Main.postgres.from("gamemodes").eq("id", Integer.parseInt(event.getValues().getFirst())).execute().get();
            Gamemode gamemode = new Gamemode(result.data.getFirst());
            PostgreSQL.QueryResult player = Main.postgres.from("players").eq("discord_id", event.getUser().getId()).execute().get();
            if (result.isEmpty()) {
                event.reply("Nem vagy fent a tierlisten!").setEphemeral(true).queue();
                return;
            }
            Map<String, Object> data = player.data.getFirst();
            JsonObject lastTest = gson.fromJson(data.get("last_test").toString(), JsonObject.class);
            String lastTestValue = lastTest.has(String.valueOf(gamemode.getId()))
                    ? lastTest.get(String.valueOf(gamemode.getId())).getAsString()
                    : "";

            long lastTestTime = 0;
            if (!lastTestValue.isEmpty()) {
                lastTestTime = Long.parseLong(lastTestValue);
            }
            if (lastTestTime + Main.testCooldown > System.currentTimeMillis()) {
                event.reply("Az újratesztelési időkereted lejár <t:" + (lastTestTime + Main.testCooldown) / 1000 + ":R>").setEphemeral(true).queue();
                return;
            }
            String tier = gson.fromJson(data.get("tiers").toString(), JsonObject.class).get(String.valueOf(gamemode.getId())).getAsString();
            if (tier.endsWith("4") || tier.endsWith("5")) {
                event.reply("Minimum LT3 kell legyél ebből a játékmódból!").setEphemeral(true).queue();
                return;
            }
            Main.guild.createTextChannel(event.getUser().getName().replaceAll("\\.", ""), gamemode.getCategory())
                    .addPermissionOverride(event.getMember(), EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.noneOf(Permission.class))
                    .addPermissionOverride(Main.guild.getPublicRole(), EnumSet.noneOf(Permission.class), EnumSet.of(Permission.VIEW_CHANNEL))
                    .addPermissionOverride(gamemode.getRole(), EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.noneOf(Permission.class))
                    .queue(channel -> {
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setTitle("Szia, " + event.getUser().getName() + "!");
                        embed.setDescription("Tiered: " + tier + ".\nKérlek, pingelj meg egy Regulatort, hogy kipörgesse, ki ellen kell játszanod.");
                        channel.sendMessage("<@" + event.getUser().getId() + ">").addEmbeds(embed.build()).queue();
                        event.reply("<#" + channel.getId() + ">").setEphemeral(true).queue();
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
