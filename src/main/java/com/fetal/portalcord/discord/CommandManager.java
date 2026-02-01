package com.fetal.portalcord.discord;

import com.fetal.portalcord.PortalCord;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class CommandManager {
    private final PortalCord plugin;
    private final Guild guild;

    public CommandManager(PortalCord plugin, Guild guild) {
        this.plugin = plugin;
        this.guild = guild;
    }

    public void registerCommands() {
        try {
            // Pehle existing commands delete karo (cache clear ho jayega)
            guild.updateCommands().queue();
            
            // Ab naye commands add karo
            CommandListUpdateAction commands = guild.updateCommands();
            
            commands.addCommands(
                Commands.slash("list", "Shows online players"),
                Commands.slash("server", "Shows server status and TPS"),
                Commands.slash("announce", "Broadcast message to Minecraft")
                    .addOption(OptionType.STRING, "message", "Message to send", true),
                Commands.slash("execute", "Run console command (Admin only)")
                    .addOption(OptionType.STRING, "command", "Command to execute", true)
            ).queue(
                success -> {
                    plugin.getLogger().info("✅ Slash commands registered successfully!");
                    plugin.getLogger().info("Total commands: " + success.size());
                },
                error -> {
                    plugin.getLogger().severe("❌ Failed to register commands: " + error.getMessage());
                }
            );
            
        } catch (Exception e) {
            plugin.getLogger().severe("❌ Error registering commands: " + e.getMessage());
            e.printStackTrace();
        }
    }
}