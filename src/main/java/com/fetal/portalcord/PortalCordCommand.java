package com.fetal.portalcord;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PortalCordCommand implements CommandExecutor {
    private final PortalCord plugin;

    public PortalCordCommand(PortalCord plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("portalcord.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "PortalCord v" + plugin.getDescription().getVersion());
            sender.sendMessage(ChatColor.YELLOW + "/pc reload " + ChatColor.GRAY + "- Reload config");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("portalcord.reload")) {
            plugin.reloadConfig();
            plugin.getConfigManager().reload();
            sender.sendMessage(ChatColor.GREEN + "PortalCord config reloaded!");
            return true;
        }

        return false;
    }
}