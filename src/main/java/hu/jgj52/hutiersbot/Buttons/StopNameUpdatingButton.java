package hu.jgj52.hutiersbot.Buttons;

import hu.jgj52.hutiersbot.Commands.UpdateNamesCommand;
import hu.jgj52.hutiersbot.Types.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class StopNameUpdatingButton extends Button {
    @Override
    public String getCustomId() {
        return "stopnameupdating";
    }

    @Override
    public String getLabel() {
        return "Stop";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.DANGER;
    }

    @Override
    public void execute(ButtonInteractionEvent event) {
        UpdateNamesCommand.stop = true;
        event.reply("Névfrissítés abbahagyva.").setEphemeral(true).queue();
    }
}
