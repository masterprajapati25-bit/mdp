package com.masterdragon;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class SpinGUI implements Listener {
    private static final int[] RING = {10, 11, 12, 14, 15, 16, 22, 21};
    private final MasterDragonPlugin plugin;
    private final ArtifactManager artifacts;
    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();
    private final Random random = new Random();

    private static final class SpinHolder implements InventoryHolder {
        private Inventory inventory;
        @Override public Inventory getInventory() { return inventory; }
    }

    public SpinGUI(MasterDragonPlugin plugin, ArtifactManager artifacts) {
        this.plugin = plugin;
        this.artifacts = artifacts;
    }

    public void start(Player player) {
        if (!player.isOnline()) return;
        if (activeTasks.containsKey(player.getUniqueId())) {
            player.sendMessage(Component.text("Your artifact spin is already running.", NamedTextColor.RED));
            return;
        }

        SpinHolder holder = new SpinHolder();
        String title = plugin.getConfig().getString("spin.title", "&5&lMASTER DRAGON")
                .replace('&', '§');
        Inventory gui = Bukkit.createInventory(holder, 27, Component.text(title));
        holder.inventory = gui;

        ItemStack border = pane(Material.PURPLE_STAINED_GLASS_PANE, " ");
        int[] borderSlots = {0,1,2,3,4,5,6,7,8,9,17,18,19,20,23,24,25,26};
        for (int slot : borderSlots) gui.setItem(slot, border);
        gui.setItem(13, pane(Material.YELLOW_STAINED_GLASS_PANE, "▼  WINNER  ▼"));
        player.openInventory(gui);

        int winnerIndex = random.nextInt(artifacts.getArtifacts().size());
        int duration = Math.max(40, plugin.getConfig().getInt("spin.duration-ticks", 100));
        int startDelay = Math.max(1, plugin.getConfig().getInt("spin.starting-interval-ticks", 2));
        int endDelay = Math.max(startDelay, plugin.getConfig().getInt("spin.ending-interval-ticks", 12));

        BukkitRunnable animation = new BukkitRunnable() {
            private int elapsed = 0;
            private int nextStep = 0;
            private int position = 0;
            private int previousSlot = -1;

            @Override public void run() {
                if (!player.isOnline()) { finish(); return; }

                if (elapsed >= duration) {
                    ArtifactManager.Artifact winner = artifacts.getArtifacts().get(winnerIndex);
                    gui.setItem(RING[position], artifacts.createArtifact(winnerIndex + 1));
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                    ItemStack reward = artifacts.createArtifact(winnerIndex + 1);
                    Map<Integer, ItemStack> overflow = player.getInventory().addItem(reward);
                    overflow.values().forEach(extra ->
                            player.getWorld().dropItemNaturally(player.getLocation(), extra));
                    String message = plugin.getConfig().getString("spin.win-message",
                                    "&aYou received &e%artifact%&a!")
                            .replace("%artifact%", winner.name()).replace('&', '§');
                    player.sendMessage(message);
                    finish();
                    return;
                }

                if (elapsed >= nextStep) {
                    if (previousSlot >= 0) gui.setItem(previousSlot, pane(Material.GRAY_STAINED_GLASS_PANE, " "));
                    position = (position + 1) % RING.length;
                    int artifactIndex = (position + elapsed / 3) % artifacts.getArtifacts().size();
                    gui.setItem(RING[position], artifacts.createArtifact(artifactIndex + 1));
                    previousSlot = RING[position];
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f,
                            Math.min(2.0f, 0.7f + (float) elapsed / duration));
                    double progress = (double) elapsed / duration;
                    int delay = (int) Math.round(startDelay + (endDelay - startDelay) * progress * progress);
                    nextStep = elapsed + Math.max(1, delay);
                }
                elapsed++;
            }

            private void finish() {
                activeTasks.remove(player.getUniqueId());
                cancel();
            }
        };

        BukkitTask task = animation.runTaskTimer(plugin, 0L, 1L);
        activeTasks.put(player.getUniqueId(), task);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof SpinHolder) {
            event.setCancelled(true);
        }
    }

    public void cancelAll() {
        activeTasks.values().forEach(BukkitTask::cancel);
        activeTasks.clear();
    }

    private static ItemStack pane(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, NamedTextColor.GRAY));
        item.setItemMeta(meta);
        return item;
    }
}
