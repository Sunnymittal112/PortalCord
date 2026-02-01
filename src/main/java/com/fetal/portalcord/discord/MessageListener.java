package com.fetal.portalcord.discord;

import com.fetal.portalcord.PortalCord;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;

import java.util.List;

public class MessageListener extends ListenerAdapter {
    private final PortalCord plugin;

    public MessageListener(PortalCord plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        
        TextChannel channel = event.getChannel().asTextChannel();
        String chatChannelId = plugin.getConfig().getString("discord.channels.chat");
        
        if (!channel.getId().equals(chatChannelId)) {
            String consoleChannelId = plugin.getConfig().getString("discord.channels.console");
            if (channel.getId().equals(consoleChannelId) && plugin.getConfig().getBoolean("console-access.enabled")) {
                handleConsoleCommand(event);
            }
            return;
        }
        
        Member member = event.getMember();
        if (member == null) return;
        
        String displayName = member.getEffectiveName();
        String message = event.getMessage().getContentDisplay();
        
        if (!event.getMessage().getAttachments().isEmpty()) {
            message += " [Image]";
        }
        
        String format = plugin.getConfig().getString("messages.discord-format", "§9[§bDiscord§9] §f{displayname}: §7{message}");
        String formatted = format.replace("{displayname}", displayName).replace("{message}", message);
        
        Component component = MiniMessage.miniMessage().deserialize(formatted.replace("§", "&"));
        Bukkit.broadcast(component);
    }
    
    private void handleConsoleCommand(MessageReceivedEvent event) {
        Member member = event.getMember();
        List<String> allowedRoles = plugin.getConfig().getStringList("console-access.allowed-roles");
        
        boolean hasPermission = member.getRoles().stream()
                .map(Role::getId)
                .anyMatch(allowedRoles::contains);
        
        if (!hasPermission) {
            event.getMessage().reply("❌ You don't have console access!").queue();
            return;
        }
        
        String command = event.getMessage().getContentRaw();
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            if (success) {
                event.getMessage().addReaction(net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("✅")).queue();
            } else {
                event.getMessage().addReaction(net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("❌")).queue();
            }
        });
    }
}