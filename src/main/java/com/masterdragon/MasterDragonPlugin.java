package com.masterdragon;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class MasterDragonPlugin extends JavaPlugin implements Listener {
    private ArtifactManager artifacts;
    private SpinGUI spinGUI;
    private final Set<UUID> startupPlayers = new HashSet<>();
    private boolean firstJoinSpinUsed = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        artifacts = new ArtifactManager(this);
        spinGUI = new SpinGUI(this, artifacts);
        CommandHandler commands = new CommandHandler(this, artifacts, spinGUI);

        getCommand("masterdragon").setExecutor(commands);
        getCommand("masterdragon").setTabCompleter(commands);
        getCommand("mdspin").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use /mdspin.");
                return true;
            }
            spinGUI.start(player);
            return true;
        });
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(spinGUI, this);

        if (getConfig().getBoolean("startup.enabled")
                && getConfig().getString("startup.mode", "first-join").equalsIgnoreCase("all-players")) {
            Bukkit.getScheduler().runTask(this, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (startupPlayers.add(player.getUniqueId())) spinGUI.start(player);
                }
            });
        }
        getLogger().info("MasterDragonPlugin enabled.");
    }

    @Override
    public void onDisable() {
        if (spinGUI != null) spinGUI.cancelAll();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!getConfig().getBoolean("startup.enabled")) return;
        String mode = getConfig().getString("startup.mode", "first-join");
        Player player = event.getPlayer();

        if (mode.equalsIgnoreCase("first-join")) {
            if (!firstJoinSpinUsed) {
                firstJoinSpinUsed = true;
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    if (player.isOnline()) spinGUI.start(player);
                }, 20L);
            }
        } else if (mode.equalsIgnoreCase("all-players")) {
            if (startupPlayers.add(player.getUniqueId())) {
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    if (player.isOnline()) spinGUI.start(player);
                }, 20L);
            }
        }
    }

    public void reloadPluginConfig() {
        reloadConfig();
    }
}
