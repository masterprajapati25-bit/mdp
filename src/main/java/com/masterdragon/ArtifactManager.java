package com.masterdragon;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CustomModelDataComponent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class ArtifactManager {
    public record Artifact(int id, String name, Material material, NamedTextColor color,
                           String tier, String description) {}

    private final MasterDragonPlugin plugin;
    private final NamespacedKey artifactKey;
    private final List<Artifact> artifacts = List.of(
        new Artifact(1001, "Dragonheart Ember", Material.FIRE_CHARGE, NamedTextColor.GOLD,
                "Ancient Dragon Relic", "A warm ember from the first dragon's heart."),
        new Artifact(1002, "Emerald Wyrm Core", Material.EMERALD, NamedTextColor.GREEN,
                "Verdant Dragon Relic", "A bright core pulsing with forest energy."),
        new Artifact(1003, "Solar Phoenix Orb", Material.MAGMA_CREAM, NamedTextColor.RED,
                "Solar Dragon Relic", "A tiny sun sealed inside a crystal sphere."),
        new Artifact(1004, "Crimson Star Shard", Material.AMETHYST_SHARD, NamedTextColor.LIGHT_PURPLE,
                "Astral Dragon Relic", "A shard carrying the glow of a distant star."),
        new Artifact(1005, "Glacial Moon Pearl", Material.PRISMARINE_CRYSTALS, NamedTextColor.AQUA,
                "Glacial Dragon Relic", "A cool pearl touched by moonlit snow."),
        new Artifact(1006, "Verdant Wind Sigil", Material.HEART_OF_THE_SEA, NamedTextColor.DARK_GREEN,
                "Tempest Dragon Relic", "A sea-green sigil filled with gentle wind."),
        new Artifact(1007, "Lunar Halo", Material.NAUTILUS_SHELL, NamedTextColor.WHITE,
                "Lunar Dragon Relic", "A pale halo that shimmers like the night sky."),
        new Artifact(1008, "Voidlight Prism", Material.NETHER_STAR, NamedTextColor.DARK_PURPLE,
                "Void Dragon Relic", "A prism holding a quiet violet light.")
    );

    public ArtifactManager(MasterDragonPlugin plugin) {
        this.plugin = plugin;
        this.artifactKey = new NamespacedKey(plugin, "masterdragon_artifact_id");
    }

    public List<Artifact> getArtifacts() { return artifacts; }

    public Artifact byNumber(int number) {
        if (number < 1 || number > artifacts.size()) return null;
        return artifacts.get(number - 1);
    }

    public ItemStack createArtifact(int number) {
        Artifact artifact = byNumber(number);
        if (artifact == null) return null;

        ItemStack item = new ItemStack(artifact.material());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(artifact.name(), artifact.color())
                .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
            Component.text("✦ " + artifact.tier(), NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false),
            Component.text(artifact.description(), NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
            Component.text("MASTER DRAGON COLLECTION", NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, false)
        ));
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        CustomModelDataComponent modelData = meta.getCustomModelDataComponent();
        modelData.setFloats(List.of((float) artifact.id()));
        meta.setCustomModelDataComponent(modelData);
        meta.getPersistentDataContainer().set(artifactKey, PersistentDataType.INTEGER, artifact.id());

        item.setItemMeta(meta);
        return item;
    }

    public boolean isArtifact(ItemStack item) {
        return item != null && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer()
                .has(artifactKey, PersistentDataType.INTEGER);
    }
}
