package de.fabilucius.advancedperks.perk.properties;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Strings;
import de.fabilucius.advancedperks.core.guisystem.HeadTexture;
import de.fabilucius.advancedperks.core.itembuilder.types.skull.SkullStackBuilder;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ItemFlag;

public class PerkGuiIcon {

    private final ItemStack itemStack;

    public PerkGuiIcon(String data) {
        if (Strings.isNullOrEmpty(data)) {
            this.itemStack = SkullStackBuilder.fromMaterial(Material.PLAYER_HEAD)
                    .setBase64Value(HeadTexture.MISSING_TEXTURE.getValue())
                    .build();
        } else {
            Optional<XMaterial> approximateMaterial = XMaterial.matchXMaterial(data);
            if (approximateMaterial.isPresent()) {
                this.itemStack = approximateMaterial.get().parseItem();
            } else if (Base64.isBase64(data)) {
                this.itemStack = SkullStackBuilder.fromMaterial(Material.PLAYER_HEAD)
                        .setBase64Value(data)
                        .build();
            } else {
                throw new IllegalArgumentException("Unable to parse the perk gui icon. The data provided is neither a valid material nor a valid base64 encoded skull texture: %s".formatted(data));
            }
        }
        hideItemMeta();
    }

    public PerkGuiIcon(Material material) {
        hideItemMeta();
        this.itemStack = new ItemStack(material);
    }

    private void hideItemMeta() {
        ItemMeta meta = this.itemStack.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
            meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            this.itemStack.setItemMeta(meta);
        }
    }

    public ItemStack getIcon() {
        return this.itemStack;
    }

}
