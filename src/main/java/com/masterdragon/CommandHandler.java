package com.masterdragon;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class CommandHandler implements CommandExecutor, TabCompleter {
    private final MasterDragonPlugin plugin;
    private final ArtifactManager artifacts;
    private final SpinGUI spin;

    public CommandHandler(MasterDragonPlugin plugin, ArtifactManager artifacts, SpinGUI spin) {
        this.plugin = plugin;
        this.artifacts = artifacts;
        this.spin = spin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text(
                    "/masterdragon spin [player] | give <1-8> [player] | reload", NamedTextColor.YELLOW));
            return true;
        }

        if (args[0].equalsIgnoreCase("spin")) {
            if (!sender.hasPermission("masterdragon.admin")) return noPermission(sender);
            Player target = args.length >= 2 ? Bukkit.getPlayerExact(args[1])
                    : (sender instanceof Player p ? p : null);
            if (target == null) {
                sender.sendMessage(Component.text("Usage: /masterdragon spin <online-player>",
                        NamedTextColor.RED));
                return true;
            }
            spin.start(target);
            sender.sendMessage(Component.text("Started an artifact spin for " + target.getName(),
                    NamedTextColor.GREEN));
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("masterdragon.admin")) return noPermission(sender);
            if (args.length < 2) {
                sender.sendMessage(Component.text("Usage: /masterdragon give <1-8> [player]",
                        NamedTextColor.RED));
                return true;
            }
            int number;
            try { number = Integer.parseInt(args[1]); }
            catch (NumberFormatException exception) {
                sender.sendMessage(Component.text("Choose an artifact number from 1 to 8.", NamedTextColor.RED));
                return true;
            }
            if (artifacts.byNumber(number) == null) {
                sender.sendMessage(Component.text("Choose an artifact number from 1 to 8.", NamedTextColor.RED));
                return true;
            }
            Player target = args.length >= 3 ? Bukkit.getPlayerExact(args[2])
                    : (sender instanceof Player p ? p : null);
            if (target == null) {
                sender.sendMessage(Component.text("Specify an online player.", NamedTextColor.RED));
                return true;
            }
            ItemStack reward = artifacts.createArtifact(number);
            target.getInventory().addItem(reward).values()
                    .forEach(extra -> target.getWorld().dropItemNaturally(target.getLocation(), extra));
            sender.sendMessage(Component.text("Gave " + artifacts.byNumber(number).name()
                    + " to " + target.getName(), NamedTextColor.GREEN));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("masterdragon.admin")) return noPermission(sender);
            plugin.reloadPluginConfig();
            sender.sendMessage(Component.text("Master Dragon config reloaded.", NamedTextColor.GREEN));
            return true;
        }
        return true;
    }

    private boolean noPermission(CommandSender sender) {
        sender.sendMessage(Component.text("You do not have permission.", NamedTextColor.RED));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("spin", "give", "reload");
        if (args.length == 2 && args[0].equalsIgnoreCase("give"))
            return List.of("1", "2", "3", "4", "5", "6", "7", "8");
        return List.of();
    }
}
