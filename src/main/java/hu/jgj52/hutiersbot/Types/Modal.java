package hu.jgj52.hutiersbot.Types;

import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import java.util.List;

public abstract class Modal {
    public abstract String getCustomId();
    public abstract String getTitle();
    public abstract List<Label> getLabels();
    public net.dv8tion.jda.api.modals.Modal modal() {
        net.dv8tion.jda.api.modals.Modal.Builder modal = net.dv8tion.jda.api.modals.Modal.create(getCustomId(), getTitle());
        for (Label label : getLabels()) {
            modal.addComponents(label);
        }
        return modal.build();
    }

    public abstract void execute(ModalInteractionEvent event);
}
