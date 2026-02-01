package com.fetal.portalcord.discord;

import com.fetal.portalcord.PortalCord;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class DiscordBot extends ListenerAdapter {
    private final PortalCord plugin;
    private JDA jda;
    private TextChannel chatChannel;
    private TextChannel consoleChannel;
    private TextChannel statusChannel;

    public DiscordBot(PortalCord plugin) {
        this.plugin = plugin;
    }

    public boolean connect() {
        try {
            String token = plugin.getConfig().getString("discord.token");
            
            jda = JDABuilder.createDefault(token)
                    .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS
                    )
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .addEventListeners(this)
                    .addEventListeners(new MessageListener(plugin))
                    .addEventListeners(new SlashCommandListener(plugin))  // YEH LINE ADD KARI HAI
                    .build();
            
            jda.awaitReady();
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Discord login failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        plugin.getLogger().info("Bot logged in as: " + event.getJDA().getSelfUser().getName());
        
        String guildId = plugin.getConfig().getString("discord.guild-id");
        Guild guild = jda.getGuildById(guildId);
        
        if (guild != null) {
            plugin.getLogger().info("Connected to guild: " + guild.getName());
            
            // Chat Channel
            String chatId = plugin.getConfig().getString("discord.channels.chat");
            if (isValidId(chatId)) {
                try {
                    chatChannel = guild.getTextChannelById(chatId);
                    if (chatChannel != null) plugin.getLogger().info("Chat channel connected!");
                } catch (Exception e) {
                    plugin.getLogger().warning("Invalid chat channel ID: " + chatId);
                }
            }
            
            // Console Channel (Optional)
            String consoleId = plugin.getConfig().getString("discord.channels.console");
            if (isValidId(consoleId)) {
                try {
                    consoleChannel = guild.getTextChannelById(consoleId);
                    if (consoleChannel != null) plugin.getLogger().info("Console channel connected!");
                } catch (Exception e) {
                    plugin.getLogger().warning("Invalid console channel ID: " + consoleId);
                }
            }
            
            // Status Channel (Optional)
            String statusId = plugin.getConfig().getString("discord.channels.status");
            if (isValidId(statusId)) {
                try {
                    statusChannel = guild.getTextChannelById(statusId);
                    if (statusChannel != null) {
                        plugin.getLogger().info("Status channel connected!");
                        startStatusUpdates();
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Invalid status channel ID: " + statusId);
                }
            }
            
            // Register Slash Commands
            if (plugin.getConfig().getBoolean("commands.slash-commands", true)) {
                new CommandManager(plugin, guild).registerCommands();
            }
        } else {
            plugin.getLogger().severe("Guild not found! Check your guild-id in config.yml");
        }
        
        updateActivity();
    }

    private boolean isValidId(String id) {
        return id != null && !id.isEmpty() && !id.equals("CHAT_CHANNEL_ID") 
               && !id.equals("CONSOLE_CHANNEL_ID") && !id.equals("STATUS_CHANNEL_ID")
               && id.matches("\\d+");  // Sirf numbers honi chahiye (18 digit)
    }

    public void sendMessageToDiscord(String content, MessageEmbed embed) {
        if (chatChannel == null) return;
        
        if (embed != null && plugin.getConfig().getBoolean("messages.use-embeds", true)) {
            chatChannel.sendMessageEmbeds(embed).queue();
        } else {
            chatChannel.sendMessage(content).queue();
        }
    }

    public void sendConsoleOutput(String message) {
        if (consoleChannel == null) return;
        consoleChannel.sendMessage("```" + message + "```").queue();
    }

    public void sendPlayerJoin(Player player) {
        String format = plugin.getConfig().getString("messages.minecraft-format.join");
        String msg = format.replace("{player}", player.getName());
        
        if (plugin.getConfig().getBoolean("messages.use-embeds", true)) {
            Color color = new Color(46, 204, 113);
            String avatarUrl = "https://crafatar.com/avatars/" + player.getUniqueId() + "?overlay=true";
            
            MessageEmbed embed = new net.dv8tion.jda.api.EmbedBuilder()
                    .setAuthor(player.getName() + " joined the server", null, avatarUrl)
                    .setColor(color)
                    .setFooter("Now online: " + Bukkit.getOnlinePlayers().size())
                    .build();
            
            sendMessageToDiscord(msg, embed);
        } else {
            sendMessageToDiscord(msg, null);
        }
        
        updateActivity();
    }

    public void sendPlayerQuit(Player player) {
        String format = plugin.getConfig().getString("messages.minecraft-format.leave");
        String msg = format.replace("{player}", player.getName());
        
        if (plugin.getConfig().getBoolean("messages.use-embeds", true)) {
            Color color = new Color(231, 76, 60);
            String avatarUrl = "https://crafatar.com/avatars/" + player.getUniqueId() + "?overlay=true";
            
            MessageEmbed embed = new net.dv8tion.jda.api.EmbedBuilder()
                    .setAuthor(player.getName() + " left the server", null, avatarUrl)
                    .setColor(color)
                    .setFooter("Now online: " + (Bukkit.getOnlinePlayers().size() - 1))
                    .build();
            
            sendMessageToDiscord(msg, embed);
        } else {
            sendMessageToDiscord(msg, null);
        }
        
        Bukkit.getScheduler().runTaskLater(plugin, this::updateActivity, 20L);
    }

    public void sendChatMessage(Player player, String message) {
        String format = plugin.getConfig().getString("messages.minecraft-format.chat");
        String content = format.replace("{player}", player.getName()).replace("{message}", message);
        
        if (plugin.getConfig().getBoolean("messages.use-embeds", true)) {
            String avatarUrl = "https://crafatar.com/avatars/" + player.getUniqueId() + "?overlay=true";
            
            MessageEmbed embed = new net.dv8tion.jda.api.EmbedBuilder()
                    .setAuthor(player.getName(), null, avatarUrl)
                    .setDescription(message)
                    .setColor(new Color(52, 152, 219))
                    .build();
            
            sendMessageToDiscord(content, embed);
        } else {
            sendMessageToDiscord(content, null);
        }
    }

    private void startStatusUpdates() {
        if (!plugin.getConfig().getBoolean("status-channel.enabled", true)) return;
        
        int interval = plugin.getConfig().getInt("status-channel.update-interval", 60);
        
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (statusChannel == null) return;
            
            String format = plugin.getConfig().getString("status-channel.format", "🎮 Players: {online}/{max}");
            String newName = format
                    .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                    .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));
            
            try {
                statusChannel.getManager().setName(newName).queue();
            } catch (Exception e) {
                // Rate limited
            }
        }, 20L * interval, 20L * interval);
    }

    public void updateActivity() {
        if (!plugin.getConfig().getBoolean("messages.activity.enabled", true)) return;
        
        String type = plugin.getConfig().getString("messages.activity.type", "WATCHING");
        String text = plugin.getConfig().getString("messages.activity.text", "{online} players online")
                .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));
        
        Activity activity = switch (type.toUpperCase()) {
            case "PLAYING" -> Activity.playing(text);
            case "LISTENING" -> Activity.listening(text);
            case "COMPETING" -> Activity.competing(text);
            default -> Activity.watching(text);
        };
        
        jda.getPresence().setActivity(activity);
    }

    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
            try {
                if (!jda.awaitShutdown(5, TimeUnit.SECONDS)) {
                    jda.shutdownNow();
                }
            } catch (InterruptedException e) {
                jda.shutdownNow();
            }
        }
    }

    public JDA getJDA() {
        return jda;
    }

    public TextChannel getChatChannel() {
        return chatChannel;
    }
}