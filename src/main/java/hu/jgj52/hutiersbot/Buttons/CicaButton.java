package hu.jgj52.hutiersbot.Buttons;

import hu.jgj52.hutiersbot.Types.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class CicaButton extends Button {
    @Override
    public String getCustomId() {
        return "cica";
    }

    @Override
    public String getLabel() {
        return "CICABGOMB";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.PRIMARY;
    }

    @Override
    public void execute(ButtonInteractionEvent event) {
        event.reply("tenyelg cica").queue();
    }
}
