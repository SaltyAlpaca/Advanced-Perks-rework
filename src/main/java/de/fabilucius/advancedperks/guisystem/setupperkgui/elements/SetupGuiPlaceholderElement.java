package de.fabilucius.advancedperks.guisystem.setupperkgui.elements;

import de.fabilucius.advancedperks.core.guisystem.GuiSound;
import de.fabilucius.advancedperks.core.guisystem.element.AbstractGuiElement;
import de.fabilucius.advancedperks.core.guisystem.element.GuiElement;
import de.fabilucius.advancedperks.core.guisystem.window.GuiWindow;
import de.fabilucius.advancedperks.core.itembuilder.types.ItemStackBuilder;
import de.fabilucius.advancedperks.guisystem.configuration.PerkGuiSaveResult;
import de.fabilucius.advancedperks.guisystem.setupperkgui.SetupPerkGuiWindow;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryAction;

import java.util.function.BiConsumer;

public class SetupGuiPlaceholderElement extends AbstractGuiElement {
    public SetupGuiPlaceholderElement(GuiWindow guiWindow) {
        super(guiWindow, ItemStackBuilder.fromMaterial(Material.CHAIN_COMMAND_BLOCK)
                .setDisplayName(ChatColor.DARK_GRAY + "Setup Gui Slot")
                .setDescription(ChatColor.GRAY + "Choose a slot where this element should be in the gui.",
                        ChatColor.GRAY + "Press " + ChatColor.AQUA + "Control + Q " + ChatColor.GRAY + "to save the changes done to the layout.",
                        ChatColor.GRAY + "Press " + ChatColor.AQUA + "Q " + ChatColor.GRAY + "to enable/disable a background for the gui.")
                .build());
    }

    @Override
    public BiConsumer<GuiElement, InventoryClickEvent> handleInventoryClick() {
        return (guiElement, event) -> {
            SetupPerkGuiWindow guiWindow = (SetupPerkGuiWindow) this.getGuiWindow();

            // Prevent item manipulation that could break the GUI setup
            if (isRestrictedAction(event)) {
                event.setCancelled(true);
                return;
            }

            if (event.getClick().equals(ClickType.DROP)) {
                guiWindow.toggleHasBackground();
                event.setCancelled(true);
                this.playSound(GuiSound.NORMAL_CLICK);
            } else if (event.getClick().equals(ClickType.CONTROL_DROP)) {
                event.setCancelled(true);
                PerkGuiSaveResult result = guiWindow.save();
                if (result.equals(PerkGuiSaveResult.SUCCESS)) {
                    event.getWhoClicked().closeInventory();
                    this.playSound(GuiSound.SETUP_CLICK);
                } else {
                    this.getGuiWindow().setTitle(result.getMessage());
                    this.playSound(GuiSound.ERROR_CLICK);
                }
            } else {
                // Cancel other click types to prevent accidental item movement
                event.setCancelled(true);
            }
        };
    }

    /**
     * Checks if the inventory action is restricted to prevent GUI corruption.
     *
     * @param event The inventory click event
     * @return true if the action should be restricted
     */
    private boolean isRestrictedAction(InventoryClickEvent event) {
        InventoryAction action = event.getAction();
        return isProblematicAction(action) ||
                isProblematicShiftClick(event);
    }

    /**
     * Checks if the inventory action is problematic.
     *
     * @param action The inventory action
     * @return true if the action is problematic
     */
    private boolean isProblematicAction(InventoryAction action) {
        return action == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                action == InventoryAction.HOTBAR_SWAP ||
                action == InventoryAction.HOTBAR_MOVE_AND_READD;
    }

    /**
     * Checks if the shift-click is problematic.
     *
     * @param event The inventory click event
     * @return true if the shift-click is problematic
     */
    private boolean isProblematicShiftClick(InventoryClickEvent event) {
        return event.isShiftClick() && !isAllowedShiftClick(event);
    }

    /**
     * Determines if a shift-click action is allowed in the setup GUI.
     *
     * @param event The inventory click event
     * @return true if the shift-click is allowed
     */
    private boolean isAllowedShiftClick(InventoryClickEvent event) {
        // Allow shift-click only for specific setup operations
        return event.getClick() == ClickType.SHIFT_LEFT ||
                event.getClick() == ClickType.SHIFT_RIGHT;
    }
}