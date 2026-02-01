package com.fetal.portalcord;

import com.fetal.portalcord.config.ConfigManager;
import com.fetal.portalcord.discord.DiscordBot;
import com.fetal.portalcord.minecraft.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

public class PortalCord extends JavaPlugin {
    private static PortalCord instance;
    private DiscordBot discordBot;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        
        // Initialize Discord Bot
        discordBot = new DiscordBot(this);
        if (!discordBot.connect()) {
            getLogger().severe("Failed to connect to Discord! Check your token.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Register events
        getServer().getPluginManager().registerEvents(new PlayerListener(this, discordBot), this);
        
        // Register command
        getCommand("portalcord").setExecutor(new PortalCordCommand(this));
        
        getLogger().info("PortalCord enabled! Bridging Minecraft ↔ Discord");
    }

    @Override
    public void onDisable() {
        if (discordBot != null) {
            discordBot.shutdown();
        }
        getLogger().info("PortalCord disabled!");
    }

    public static PortalCord getInstance() {
        return instance;
    }

    public DiscordBot getDiscordBot() {
        return discordBot;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}