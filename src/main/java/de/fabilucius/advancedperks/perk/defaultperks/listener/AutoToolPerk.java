package de.fabilucius.advancedperks.perk.defaultperks.listener;

import com.google.inject.Inject;
import de.fabilucius.advancedperks.data.PerkData;
import de.fabilucius.advancedperks.data.PerkDataRepository;
import de.fabilucius.advancedperks.perk.AbstractDefaultPerk;
import de.fabilucius.advancedperks.perk.annotation.PerkIdentifier;
import de.fabilucius.advancedperks.perk.properties.PerkDescription;
import de.fabilucius.advancedperks.perk.properties.PerkGuiIcon;
import de.fabilucius.advancedperks.perk.types.ListenerPerk;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@PerkIdentifier("auto_tool")
public class AutoToolPerk extends AbstractDefaultPerk implements ListenerPerk {

    private static final Set<Material> DIGGABLE_BLOCKS = new HashSet<>();
    private static final Set<Material> MINING_BLOCKS = new HashSet<>();
    private static final Set<Material> WOOD_BLOCKS = new HashSet<>();
    private static final Set<Material> SHEARABLE_BLOCKS = new HashSet<>();
    private static final Set<Material> SWORDS = Set.of(
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD,
            Material.DIAMOND_SWORD, Material.NETHERITE_SWORD
    );
    private static final Set<EntityType> EVIL_MOBS = Set.of(
            EntityType.ZOMBIE,
            EntityType.SKELETON,
            EntityType.CREEPER,
            EntityType.SPIDER,
            EntityType.ENDERMAN,
            EntityType.WITCH,
            EntityType.HUSK,
            EntityType.STRAY,
            EntityType.DROWNED,
            EntityType.PILLAGER,
            EntityType.VINDICATOR,
            EntityType.EVOKER
    );
    static {
        for (Material material : Material.values()) {
            String name = material.name().toUpperCase();

            if (name.endsWith("_ORE") || name.contains("STONE") || name.contains("DEEPSLATE") || name.contains("END") || name.contains("CORAL_BLOCK")
                    || "NETHERRACK".equals(name) || "END_STONE".equals(name) || name.contains("TUFF") || name.contains("DIORITE") || name.contains("GRANITE") || "ANCIENT_DEBRIS".equals(name)
                    || "GLOWSTONE".equals(name) || name.contains("TERRACOTTA") || name.contains("CONCRETE") && !name.contains("CONCRETE_POWDER")
                    || name.contains("BRICKS") || "AMETHYST_BLOCK".equals(name) || name.contains("SCULK")
                    || name.contains("COPPER") || name.contains("COAL") || name.contains("IRON")
                    || name.contains("DIAMOND") || name.contains("GOLD") || name.contains("LAPIS")
                    || name.contains("EMERALD") || name.contains("REDSTONE") || name.contains("NETHERITE")
                    || name.contains("QUARTZ") || name.endsWith("_WALL") || name.endsWith("_STAIRS") && (
                    name.contains("STONE") || name.contains("BRICK") || name.contains("QUARTZ") || name.contains("NETHER")
                            || name.contains("PRISMARINE") || name.contains("ANDESITE") || name.contains("DIORITE")
                            || name.contains("GRANITE") || name.contains("BLACKSTONE") || name.contains("POLISHED"))) {
                MINING_BLOCKS.add(material);
            } else if (name.endsWith("_LOG") || name.endsWith("_WOOD") || name.endsWith("_PLANKS") || name.contains("STEM")
                    || name.startsWith("STRIPPED_") || name.contains("BAMBOO") || name.endsWith("_STAIRS") && (
                    name.contains("OAK") || name.contains("SPRUCE") || name.contains("BIRCH") || name.contains("JUNGLE")
                            || name.contains("ACACIA") || name.contains("DARK_OAK") || name.contains("CRIMSON") || name.contains("WARPED")
                            || name.contains("MANGROVE")) || "BOOKSHELF".equals(name) || "CHEST".equals(name)
                    || "CRAFTING_TABLE".equals(name) || "BARREL".equals(name) || "LADDER".equals(name)
                    || "LECTERN".equals(name) || "LOOM".equals(name) || "FLETCHING_TABLE".equals(name)
                    || "BEEHIVE".equals(name) || "BEE_NEST".equals(name)) {
                WOOD_BLOCKS.add(material);
            } else if (name.contains("DIRT") || name.contains("SAND") || name.contains("GRAVEL") || name.contains("CLAY")
                    || name.contains("MUD") || name.contains("SNOW") || name.contains("FARMLAND")
                    || name.contains("MOSS") || name.contains("SOUL") || name.contains("POWDER")) {
                DIGGABLE_BLOCKS.add(material);
            } else if (name.endsWith("_WOOL") || name.contains("LEAVES") || name.contains("CORAL") && !name.contains("CORAL_BLOCK") // Added CORAL here
                    || "COBWEB".equals(name) || "VINE".equals(name) || "GRASS".contains(name)
                    || "FERN".equals(name) || name.contains("DEAD_BUSH")
                    || name.contains("HANGING_ROOTS") || "GLOW_LICHEN".equals(name)) {
                SHEARABLE_BLOCKS.add(material);
            }
        }
    }

    @Inject
    private PerkDataRepository perkDataRepository;

    public AutoToolPerk(String identifier, String displayName, PerkDescription perkDescription, PerkGuiIcon perkGuiIcon, boolean enabled, Map<String, Object> flags) {
        super(identifier, displayName, perkDescription, perkGuiIcon, enabled, flags);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        PerkData perkData = perkDataRepository.getPerkDataByPlayer(player);

        if (!perkData.getEnabledPerks().contains(this)) {
            return;
        }

        Material blockType = event.getClickedBlock().getType();
        int bestToolSlot = findBestToolSlot(player, blockType);

        if (bestToolSlot != -1 && bestToolSlot != player.getInventory().getHeldItemSlot()) {
            ItemStack currentItem = player.getInventory().getItemInMainHand();
            ItemStack bestTool = player.getInventory().getItem(bestToolSlot);

            player.getInventory().setItemInMainHand(bestTool);
            player.getInventory().setItem(bestToolSlot, currentItem);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PerkData perkData = perkDataRepository.getPerkDataByPlayer(player);

        if (!perkData.getEnabledPerks().contains(this)) {
            return;
        }

        Entity target = getTargetEntity(player, 5);
        if (target != null) {
            int bestSwordSlot = findBestSwordSlot(player);

            if (bestSwordSlot != -1 && bestSwordSlot != player.getInventory().getHeldItemSlot()) {
                ItemStack currentItem = player.getInventory().getItemInMainHand();
                ItemStack bestSword = player.getInventory().getItem(bestSwordSlot);

                player.getInventory().setItemInMainHand(bestSword);
                player.getInventory().setItem(bestSwordSlot, currentItem);
            }
        }
    }

    private Entity getTargetEntity(Player player, int range) {
        Vector direction = player.getLocation().getDirection();
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity != null && EVIL_MOBS.contains(entity.getType())) { // Check if the entity is an "evil mob"
                Vector toEntity = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                if (direction.dot(toEntity) > 0.95) { // 0.95 is a threshold for "looking at"
                    return entity;
                }
            }
        }
        return null;
    }


    private int findBestToolSlot(Player player, Material blockType) {
        int bestToolSlot = -1;
        double bestSpeed = 0;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null) {
                continue;
            }

            double toolSpeed = getToolSpeed(item, blockType);
            if (toolSpeed > bestSpeed) {
                bestSpeed = toolSpeed;
                bestToolSlot = i;
            }
        }
        return bestToolSlot;
    }

    private int findBestSwordSlot(Player player) {
        int bestSwordSlot = -1;
        double bestDamage = 0;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || !SWORDS.contains(item.getType())) {
                continue;
            }

            double damage = getMaterialEfficiency(item.getType());
            if (damage > bestDamage) {
                bestDamage = damage;
                bestSwordSlot = i;
            }
        }
        return bestSwordSlot;
    }

    private double getToolSpeed(ItemStack tool, Material blockType) {
        Material toolType = tool.getType();
        return isToolEffective(toolType, blockType) ? getMaterialEfficiency(toolType) : 0;
    }

    private boolean isToolEffective(Material toolType, Material blockType) {
        return isEffectivePickaxe(toolType, blockType)
                || isEffectiveAxe(toolType, blockType)
                || isEffectiveShovel(toolType, blockType)
                || isEffectiveShears(toolType, blockType);
    }

    private boolean isEffectivePickaxe(Material toolType, Material blockType) {
        return isPickaxe(toolType) && MINING_BLOCKS.contains(blockType);
    }

    private boolean isEffectiveAxe(Material toolType, Material blockType) {
        return isAxe(toolType) && WOOD_BLOCKS.contains(blockType);
    }

    private boolean isEffectiveShovel(Material toolType, Material blockType) {
        return isShovel(toolType) && DIGGABLE_BLOCKS.contains(blockType);
    }

    private boolean isEffectiveShears(Material toolType, Material blockType) {
        return isShears(toolType) && SHEARABLE_BLOCKS.contains(blockType);
    }

    private boolean isPickaxe(Material material) {
        return material.name().endsWith("_PICKAXE");
    }

    private boolean isAxe(Material material) {
        return material.name().endsWith("_AXE");
    }

    private boolean isShovel(Material material) {
        return material.name().endsWith("_SHOVEL");
    }

    private boolean isShears(Material material) {
        return material == Material.SHEARS;
    }

    private double getMaterialEfficiency(Material material) {
        return switch (material) {
            case NETHERITE_PICKAXE, NETHERITE_AXE, NETHERITE_SHOVEL, NETHERITE_SWORD -> 12.0;
            case DIAMOND_PICKAXE, DIAMOND_AXE, DIAMOND_SHOVEL, DIAMOND_SWORD -> 10.0;
            case IRON_PICKAXE, IRON_AXE, IRON_SHOVEL, IRON_SWORD -> 8.0;
            case GOLDEN_PICKAXE, GOLDEN_AXE, GOLDEN_SHOVEL, GOLDEN_SWORD -> 9.0;
            case STONE_PICKAXE, STONE_AXE, STONE_SHOVEL, STONE_SWORD -> 6.0;
            case WOODEN_PICKAXE, WOODEN_AXE, WOODEN_SHOVEL, WOODEN_SWORD -> 4.0;
            case SHEARS -> 5.0;
            default -> 0;
        };
    }
}
