package com.fetal.portalcord.config;

import com.fetal.portalcord.PortalCord;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final PortalCord plugin;
    private FileConfiguration config;

    public ConfigManager(PortalCord plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public String getString(String path) {
        return config.getString(path, "");
    }

    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }

    public int getInt(String path) {
        return config.getInt(path, 0);
    }

    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path, false);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}