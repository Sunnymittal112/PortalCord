package com.fetal.portalcord.minecraft;

import com.fetal.portalcord.PortalCord;
import com.fetal.portalcord.discord.DiscordBot;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class PlayerListener implements Listener {
    private final PortalCord plugin;
    private final DiscordBot discordBot;

    public PlayerListener(PortalCord plugin, DiscordBot discordBot) {
        this.plugin = plugin;
        this.discordBot = discordBot;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        
        List<String> blocked = plugin.getConfig().getStringList("filters.blocked-words");
        if (blocked.stream().anyMatch(message::contains)) {
            return;
        }
        
        discordBot.sendChatMessage(player, message);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        discordBot.sendPlayerJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        discordBot.sendPlayerQuit(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        String msg = event.getDeathMessage();
        if (msg == null) return;
        discordBot.sendMessageToDiscord(":skull: " + msg, null);
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        String key = event.getAdvancement().getKey().getKey();
        if (key.contains("recipe/")) return;
        
        String player = event.getPlayer().getName();
        String advancement = key.substring(key.lastIndexOf("/") + 1).replace("_", " ");
        
        String format = plugin.getConfig().getString("messages.minecraft-format.advancement");
        String msg = format.replace("{player}", player).replace("{advancement}", advancement);
        
        discordBot.sendMessageToDiscord(msg, null);
    }
}