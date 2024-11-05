package de.fabilucius.advancedperks.guisystem.perkgui.elements;

import com.google.inject.Inject;
import de.fabilucius.advancedperks.AdvancedPerks;
import de.fabilucius.advancedperks.core.configuration.type.MessageConfiguration;
import de.fabilucius.advancedperks.core.guisystem.GuiSound;
import de.fabilucius.advancedperks.core.guisystem.element.AbstractGuiElement;
import de.fabilucius.advancedperks.core.guisystem.element.GuiElement;
import de.fabilucius.advancedperks.core.guisystem.window.GuiWindow;
import de.fabilucius.advancedperks.core.itembuilder.types.ItemStackBuilder;
import de.fabilucius.advancedperks.data.state.PerkStateController;
import de.fabilucius.advancedperks.data.state.PerkToggleResult;
import de.fabilucius.advancedperks.perk.Perk;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;
import java.util.List;
import java.util.Collections;

public class PerkToggleElement extends AbstractGuiElement {

    @Inject
    private PerkStateController perkStateController;

    @Inject
    private AdvancedPerks advancedPerks;

    private final MessageConfiguration messageConfiguration;
    private final Perk perk;
    private final String enabledText;
    private final String disabledText;

    public PerkToggleElement(GuiWindow guiWindow, MessageConfiguration messageConfiguration, Perk perk, boolean enabled, boolean unlocked, String notUnlockedText, String enabledText, String disabledText) {
        super(guiWindow, createInitialIcon(messageConfiguration, unlocked, enabled, notUnlockedText, enabledText, disabledText));
        this.messageConfiguration = messageConfiguration;
        this.perk = perk;
        this.enabledText = enabledText;
        this.disabledText = disabledText;
    }

    private static ItemStack createInitialIcon(MessageConfiguration config, boolean unlocked, boolean enabled, String notUnlockedText, String enabledText, String disabledText) {
        Material material = !unlocked ? Material.IRON_BARS : enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        String displayName = !unlocked ? notUnlockedText : enabled ? enabledText : disabledText;

        // Get and color lore based on status
        List<String> lore = getLoreFromConfig(config, !unlocked ? "gui.perk_gui.lores.locked" : enabled ? "gui.perk_gui.lores.enabled" : "gui.perk_gui.lores.disabled");

        // Build the item with display name and lore (if available)
        ItemStackBuilder builder = ItemStackBuilder.fromMaterial(material).setDisplayName(displayName);
        if (!lore.isEmpty()) {
            builder.setDescription(lore);
        }

        return builder.build();
    }

    @Override
    public BiConsumer<GuiElement, InventoryClickEvent> handleInventoryClick() {
        return (guiElement, event) -> {
            event.setCancelled(true);
            PerkToggleResult result = perkStateController.togglePerk(getGuiWindow().getPlayer(), perk);

            handleToggleResult(result);
        };
    }

    private void handleToggleResult(PerkToggleResult result) {
        String resultMessageKey = "gui.perk_gui.toggle." + result.name().toLowerCase();

        switch (result) {
            case EVENT_CANCELLED, DISALLOWED_WORLD, NO_PERMISSION, TOO_MANY_ACTIVE -> {
                updateTitle(resultMessageKey);
                playSound(GuiSound.ERROR_CLICK);
            }
            case ENABLED -> updateIcon(Material.LIME_DYE, enabledText, "gui.perk_gui.lores.enabled", GuiSound.ON_CLICK);
            case DISABLED -> updateIcon(Material.GRAY_DYE, disabledText, "gui.perk_gui.lores.disabled", GuiSound.OFF_CLICK);
        }
    }

    private void updateTitle(String messageKey) {
        getGuiWindow().setTitle(messageConfiguration.getMessage(messageKey));
        Bukkit.getScheduler().runTaskLater(advancedPerks, () ->
                getGuiWindow().setTitle(messageConfiguration.getMessage("gui.perk_gui.title")), 20L);
    }

    private void updateIcon(Material material, String displayName, String loreKey, GuiSound sound) {
        List<String> lore = getLoreFromConfig(messageConfiguration, loreKey);

        // Set updated item with lore (if available) and play the respective sound
        ItemStackBuilder builder = ItemStackBuilder.fromMaterial(material).setDisplayName(displayName);
        if (!lore.isEmpty()) {
            builder.setDescription(lore);
        }

        setIconAndUpdate(builder.build());
        playSound(sound);
    }

    private static List<String> getLoreFromConfig(MessageConfiguration config, String key) {
        List<String> lore = config.getStringList(key);
        if (lore.isEmpty() || lore.size() == 1 && lore.get(0).isEmpty()) {
            return Collections.emptyList();
        }
        return applyColorCodes(lore); // Apply color codes before returning
    }


    private static List<String> applyColorCodes(List<String> lore) {
        return lore.stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line)) // Convert '&' to color codes
                .toList();
    }
}
