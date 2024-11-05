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

@PerkIdentifier("lucky")
public class LuckPerk extends AbstractDefaultPerk implements EffectPerk {

    public LuckPerk(String identifier, String displayName, PerkDescription perkDescription, PerkGuiIcon perkGuiIcon, boolean enabled, Map<String, Object> flags) {
        super(identifier, displayName, perkDescription, perkGuiIcon, enabled, flags);
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        // Applies a Luck effect to improve fishing rewards
        return Lists.newArrayList(new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 0, false, false));
    }
}
