package de.fabilucius.advancedperks.perk.defaultperks.effect;

import com.google.common.collect.Lists;
import de.fabilucius.advancedperks.perk.AbstractDefaultPerk;
import de.fabilucius.advancedperks.perk.annotation.PerkIdentifier;
import de.fabilucius.advancedperks.perk.properties.PerkDescription;
import de.fabilucius.advancedperks.perk.properties.PerkGuiIcon;
import de.fabilucius.advancedperks.perk.types.EffectPerk;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;

@PerkIdentifier("haste")
public class HastePerk extends AbstractDefaultPerk implements EffectPerk {

    public HastePerk(String identifier, String displayName, PerkDescription perkDescription, PerkGuiIcon perkGuiIcon, boolean enabled, Map<String, Object> flags) {
        super(identifier, displayName, perkDescription, perkGuiIcon, enabled, flags);
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        // Applies a Haste effect with a high duration and level 1 intensity
        return Lists.newArrayList(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 0, false, false));
    }
}
