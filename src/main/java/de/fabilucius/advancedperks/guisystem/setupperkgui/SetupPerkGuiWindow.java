package de.fabilucius.advancedperks.guisystem.setupperkgui;

import com.google.inject.Inject;
import de.fabilucius.advancedperks.core.configuration.type.PerkGuiConfiguration;
import de.fabilucius.advancedperks.core.guisystem.element.GuiElement;
import de.fabilucius.advancedperks.core.guisystem.element.defaultelements.GuiBackgroundElement;
import de.fabilucius.advancedperks.core.guisystem.window.AbstractGuiWindow;
import de.fabilucius.advancedperks.guisystem.configuration.PerkGuiSaveResult;
import de.fabilucius.advancedperks.guisystem.configuration.PerkIconLocation;
import de.fabilucius.advancedperks.guisystem.setupperkgui.elements.CloseGuiPlaceholderElement;
import de.fabilucius.advancedperks.guisystem.setupperkgui.elements.DisableAllPerksPlaceholderElement;
import de.fabilucius.advancedperks.guisystem.setupperkgui.elements.NextPagePlaceholderElement;
import de.fabilucius.advancedperks.guisystem.setupperkgui.elements.PerkIconPlaceholderElement;
import de.fabilucius.advancedperks.guisystem.setupperkgui.elements.PerkTogglePlaceholderElement;
import de.fabilucius.advancedperks.guisystem.setupperkgui.elements.PreviousPagePlaceholderElement;
import de.fabilucius.advancedperks.guisystem.setupperkgui.elements.SetupGuiPlaceholderElement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class SetupPerkGuiWindow extends AbstractGuiWindow {

    private final PerkGuiConfiguration perkGuiConfiguration;
    private boolean background;

    @Inject
    public SetupPerkGuiWindow(PerkGuiConfiguration perkGuiConfiguration, Player player, boolean sounds) {
        super(Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "Click on Setup Icon to save."), player, sounds);
        this.perkGuiConfiguration = perkGuiConfiguration;
        this.background = perkGuiConfiguration.hasBackground();
    }

    @Override
    public void initializeGui() {
        if (this.hasBackground()) {
            for (int i = 0; i < this.getInventory().getSize(); i++) {
                this.addGuiElement(new GuiBackgroundElement(this), i);
            }
        }

        // Place PerkIcon and PerkToggle elements at specific slots
        List<PerkIconLocation> perkIconLocations = this.perkGuiConfiguration.getPerkIconLocations();
        for (int i = 0; i < perkIconLocations.size(); i++) {
            PerkIconLocation location = perkIconLocations.get(i);
            this.addGuiElement(new PerkIconPlaceholderElement(this, i), location.iconSlot());
            this.addGuiElement(new PerkTogglePlaceholderElement(this, i), location.toggleSlot());
        }

        this.addGuiElement(new PreviousPagePlaceholderElement(this), this.perkGuiConfiguration.getPreviousPageSlot());
        this.addGuiElement(new NextPagePlaceholderElement(this), this.perkGuiConfiguration.getNextPageSlot());
        this.addGuiElement(new CloseGuiPlaceholderElement(this), this.perkGuiConfiguration.getCloseGuiSlot());
        this.addGuiElement(new DisableAllPerksPlaceholderElement(this), this.perkGuiConfiguration.getDisableAllPerksSlot());
        this.addGuiElement(new SetupGuiPlaceholderElement(this), this.perkGuiConfiguration.getSetupGuiSlot());
    }

    public PerkGuiSaveResult save() {
        try {
            Map<Integer, Integer> perkIconLocations = new HashMap<>();
            Map<Integer, Integer> perkToggleLocations = new HashMap<>();

            // Retrieve PerkIcon and PerkToggle slots based on their initialized order
            List<GuiElement> guiElements = new ArrayList<>(this.getGuiElements().values());
            guiElements.sort(Comparator.comparingInt(this::getSlot)); // Sort by slot order

            int iconIndex = 0;
            int toggleIndex = 0;
            for (GuiElement element : guiElements) {
                int slot = this.getSlot(element);

                if (element instanceof PerkIconPlaceholderElement) {
                    perkIconLocations.put(iconIndex++, slot);
                } else if (element instanceof PerkTogglePlaceholderElement) {
                    perkToggleLocations.put(toggleIndex++, slot);
                }
            }

            // Update configuration with structured mappings
            perkGuiConfiguration.setPerkIconLocations(perkIconLocations);
            perkGuiConfiguration.setPerkToggleLocations(perkToggleLocations);

            // Save other settings and slots
            perkGuiConfiguration.setPreviousPageSlot(getSlotFromElement(PreviousPagePlaceholderElement.class, perkGuiConfiguration.getPreviousPageSlot()));
            perkGuiConfiguration.setNextPageSlot(getSlotFromElement(NextPagePlaceholderElement.class, perkGuiConfiguration.getNextPageSlot()));
            perkGuiConfiguration.setCloseGuiSlot(getSlotFromElement(CloseGuiPlaceholderElement.class, perkGuiConfiguration.getCloseGuiSlot()));
            perkGuiConfiguration.setDisableAllPerksSlot(getSlotFromElement(DisableAllPerksPlaceholderElement.class, perkGuiConfiguration.getDisableAllPerksSlot()));
            perkGuiConfiguration.setSetupGuiSlot(getSlotFromElement(SetupGuiPlaceholderElement.class, perkGuiConfiguration.getSetupGuiSlot()));
            perkGuiConfiguration.setBackground(this.hasBackground());

            perkGuiConfiguration.saveConfiguration();
            return PerkGuiSaveResult.SUCCESS;

        } catch (Exception e) {
            e.printStackTrace();
            return PerkGuiSaveResult.ERROR;
        }
    }

    // Helper method to get slot or fallback if not present
    private int getSlotFromElement(Class<? extends GuiElement> elementClass, int defaultSlot) {
        return this.getGuiElements().values().stream()
                .filter(elementClass::isInstance)
                .mapToInt(this::getSlot)
                .findFirst()
                .orElse(defaultSlot);
    }

    public boolean hasBackground() {
        return background;
    }

    public void toggleHasBackground() {
        this.background = !this.background;
        if (this.background) {
            for (int i = 0; i < this.getInventory().getSize(); i++) {
                int finalI = i;
                if (this.getGuiElements().values().stream().noneMatch(guiElement -> this.getSlot(guiElement) == finalI)) {
                    this.addGuiElement(new GuiBackgroundElement(this), i);
                }
            }
        } else {
            List<GuiElement> toRemove = this.getGuiElements().values().stream()
                    .filter(GuiBackgroundElement.class::isInstance)
                    .toList();
            toRemove.forEach(this::removeGuiElement);
        }
    }
}
