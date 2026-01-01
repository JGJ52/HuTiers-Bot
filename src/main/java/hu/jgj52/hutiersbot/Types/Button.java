package hu.jgj52.hutiersbot.Types;

import hu.jgj52.hutiersbot.Main;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public abstract class Button {
    public abstract String getCustomId();
    public abstract String getLabel();
    public abstract ButtonStyle getStyle();

    public net.dv8tion.jda.api.components.buttons.Button button() {
        return net.dv8tion.jda.api.components.buttons.Button.of(getStyle(), getCustomId(), getLabel());
    }

    public abstract void execute(ButtonInteractionEvent event);
}
