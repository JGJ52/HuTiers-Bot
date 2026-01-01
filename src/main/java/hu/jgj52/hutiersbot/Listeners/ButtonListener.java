package hu.jgj52.hutiersbot.Listeners;

import hu.jgj52.hutiersbot.Main;
import hu.jgj52.hutiersbot.Types.Button;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Objects;

public class ButtonListener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getCustomId();
        for (Button button : Main.buttons) {
            if (Objects.equals(button.getCustomId(), id)) {
                button.execute(event);
                break;
            }
        }
    }
}
