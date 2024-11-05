package de.fabilucius.advancedperks.core.configuration.type

import com.google.inject.Inject
import com.google.inject.Singleton
import com.google.inject.name.Named
import de.fabilucius.advancedperks.core.configuration.AbstractConfiguration
import de.fabilucius.advancedperks.core.configuration.annotation.FilePathInJar
import de.fabilucius.advancedperks.core.logging.APLogger
import de.fabilucius.advancedperks.guisystem.configuration.PerkIconLocation
import java.io.File

@Singleton
@FilePathInJar("perk_gui.yml")
class PerkGuiConfiguration @Inject constructor(
    @Named("configurationDirectory") configDir: File,
    logger: APLogger
) : AbstractConfiguration(configDir, logger) {

    // Method to get perk icon locations as a list of PerkIconLocation objects
    fun getPerkIconLocations(): List<PerkIconLocation> {
        val iconSlots = getIntegerList("perk_icon_locations")  // Retrieves list of integers
        val toggleSlots = getIntegerList("perk_toggle_locations")  // Retrieves list of integers

        // If either list is missing or has an unexpected size, use the default locations
        return if (iconSlots.size != 8 || toggleSlots.size != 8) {
            getDefaultPerkIconLocations()
        } else {
            iconSlots.zip(toggleSlots) { iconSlot, toggleSlot ->
                PerkIconLocation(iconSlot, toggleSlot)
            }
        }
    }

    // Default locations for perk icons and toggles
    private fun getDefaultPerkIconLocations(): List<PerkIconLocation> {
        val iconLocations = listOf(1, 3, 5, 7, 19, 21, 23, 25)
        val toggleLocations = listOf(10, 12, 14, 16, 28, 30, 32, 34)
        return iconLocations.zip(toggleLocations) { iconSlot, toggleSlot ->
            PerkIconLocation(iconSlot, toggleSlot)
        }
    }

    // Getters for other slots
    fun getCloseGuiSlot(): Int = getInt("close_gui")
    fun getDisableAllPerksSlot(): Int = getInt("disable_all_perks")
    fun getSetupGuiSlot(): Int = getInt("setup_gui")
    fun getPreviousPageSlot(): Int = getInt("previous_page")
    fun getNextPageSlot(): Int = getInt("next_page")
    fun hasBackground(): Boolean = getBoolean("background")

    fun setPerkIconLocations(perkIconLocations: Map<Int, Int>) {
        // Sort by the slot index to maintain order and save as list
        val iconSlots = perkIconLocations.keys.sorted().map { perkIconLocations[it] }
        this["perk_icon_locations"] = iconSlots
    }

    fun setPerkToggleLocations(perkToggleLocations: Map<Int, Int>) {
        // Sort by the slot index to maintain order and save as list
        val toggleSlots = perkToggleLocations.keys.sorted().map { perkToggleLocations[it] }
        this["perk_toggle_locations"] = toggleSlots
    }
    fun setPreviousPageSlot(slot: Int) {
        this["previous_page"] = slot
    }

    fun setNextPageSlot(slot: Int) {
        this["next_page"] = slot
    }

    fun setCloseGuiSlot(slot: Int) {
        this["close_gui"] = slot
    }

    fun setDisableAllPerksSlot(slot: Int) {
        this["disable_all_perks"] = slot
    }

    fun setSetupGuiSlot(slot: Int) {
        this["setup_gui"] = slot
    }

    fun setBackground(background: Boolean) {
        this["background"] = background
    }

}
