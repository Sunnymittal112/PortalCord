package com.fetal.portalcord.discord;

import com.fetal.portalcord.PortalCord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.awt.*;
import java.lang.management.ManagementFactory;  // YEH IMPORT ADD KARA
import java.util.List;
import java.util.stream.Collectors;

public class SlashCommandListener extends ListenerAdapter {
    private final PortalCord plugin;

    public SlashCommandListener(PortalCord plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();
        
        switch (command) {
            case "list":
                handleListCommand(event);
                break;
            case "server":
                handleServerCommand(event);
                break;
            case "announce":
                handleAnnounceCommand(event);
                break;
            case "execute":
                handleExecuteCommand(event);
                break;
        }
    }

    private void handleListCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
        List<String> players = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
        
        String playerList = players.isEmpty() ? "No players online" : String.join(", ", players);
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🎮 Online Players")
                .setDescription("**" + players.size() + "/" + Bukkit.getMaxPlayers() + "** players online")
                .addField("Players", playerList, false)
                .setColor(new Color(46, 204, 113))
                .setFooter("Server: " + Bukkit.getServer().getName());
        
        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }

    private void handleServerCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
        double tps = Bukkit.getServer().getTPS()[0];
        
        // FIX: JVM uptime use karo instead of Bukkit method
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        String uptimeStr = formatUptime(uptime);
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📊 Server Status")
                .addField("Version", Bukkit.getVersion(), true)
                .addField("TPS", String.format("%.1f", tps), true)
                .addField("Online", Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers(), true)
                .addField("Uptime", uptimeStr, false)
                .setColor(tps > 18 ? new Color(46, 204, 113) : new Color(231, 76, 60))
                .setTimestamp(java.time.Instant.now());
        
        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }

    private void handleAnnounceCommand(SlashCommandInteractionEvent event) {
        if (!hasAdminRole(event)) {
            event.reply("❌ You don't have permission to use this command!").setEphemeral(true).queue();
            return;
        }
        
        OptionMapping messageOption = event.getOption("message");
        if (messageOption == null) {
            event.reply("❌ Message required!").setEphemeral(true).queue();
            return;
        }
        
        String message = messageOption.getAsString();
        Bukkit.broadcastMessage("§9[§bDiscord§9] §6Announcement: §f" + message);
        
        event.reply("✅ Announcement sent to Minecraft!").queue();
    }

    private void handleExecuteCommand(SlashCommandInteractionEvent event) {
        if (!hasConsoleRole(event)) {
            event.reply("❌ You don't have console access!").setEphemeral(true).queue();
            return;
        }
        
        OptionMapping cmdOption = event.getOption("command");
        if (cmdOption == null) {
            event.reply("❌ Command required!").setEphemeral(true).queue();
            return;
        }
        
        String command = cmdOption.getAsString();
        event.deferReply().queue();
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            
            if (success) {
                event.getHook().sendMessage("✅ Command executed: `" + command + "`").queue();
            } else {
                event.getHook().sendMessage("❌ Command failed: `" + command + "`").queue();
            }
        });
    }

    private boolean hasAdminRole(SlashCommandInteractionEvent event) {
        List<String> adminRoles = plugin.getConfig().getStringList("commands.admin-roles");
        return event.getMember().getRoles().stream()
                .anyMatch(role -> adminRoles.contains(role.getId()));
    }

    private boolean hasConsoleRole(SlashCommandInteractionEvent event) {
        if (!plugin.getConfig().getBoolean("console-access.enabled", false)) return false;
        List<String> consoleRoles = plugin.getConfig().getStringList("console-access.allowed-roles");
        return event.getMember().getRoles().stream()
                .anyMatch(role -> consoleRoles.contains(role.getId()));
    }

    private String formatUptime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) return days + "d " + (hours % 24) + "h";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        return minutes + "m";
    }
}