## PortalCord - Modern Discord Bridge for Paper 1.21

**PortalCord** is a next-generation Discord bridge plugin designed specifically for Paper 1.21 servers. Unlike traditional bridges that spam plain text walls, PortalCord utilizes Discord's modern Embed API to deliver rich, visually appealing messages with player avatars, color-coded status updates, and interactive slash commands. Built with performance in mind using async operations and Paper's Adventure API, it ensures zero server lag while maintaining seamless two-way communication between your Minecraft server and Discord community.

---

### 🎯 Core Features

**Rich Discord Embeds**
Every player join, leave, death, and advancement is displayed as a beautiful Discord embed featuring the player's skin avatar (via Crafatar), custom colors (green for joins, red for leaves), and formatted timestamps. No more ugly plain text spam in your Discord channels.

**Modern Slash Commands**
Full Discord slash command integration replacing outdated prefix commands (!help):
- `/list` - Displays all online players with their ping counts in a formatted embed
- `/server` - Shows real-time TPS, server version, uptime, and player count
- `/announce &lt;message&gt;` - Broadcasts messages from Discord to all online Minecraft players with announcement formatting
- `/execute &lt;command&gt;` - Execute console commands remotely from Discord (highly restricted)

**Live Player Counter**
Automatically updates a Discord channel name (voice or text) to reflect current server population in real-time. Displays as "🎮 Players: 12/100" and updates every 60 seconds (configurable). Helps Discord members see server status at a glance without joining Minecraft.

**Two-Way Chat Synchronization**
- Minecraft → Discord: Chat messages sent in-game appear in Discord with player skin avatars and formatted text
- Discord → Minecraft: Messages sent in the linked channel appear in-game with [Discord] prefix and full MiniMessage color code support (RGB colors, gradients, hover events)

**Advanced Security & Permissions**
- **Role-Based Access Control**: Discord commands are locked behind specific Role IDs. Only users with designated admin roles can use `/announce` or `/execute`
- **Command Blacklist**: Automatically filters sensitive commands (/login, /password, /lp user) from appearing in Discord chat to prevent credential leaks
- **Word Filtering**: Built-in profanity filter that blocks specified words in both directions (Discord → MC and MC → Discord)
- **Secure Console Access**: Remote console access disabled by default. Must be explicitly enabled and restricted to specific trusted roles

**Paper 1.21 Native**
Built specifically for Paper 1.21+ using the Adventure API for modern chat handling. Supports MiniMessage format serialization, async event handling to prevent main thread blocking, and Java 21 optimizations.

---

### 📋 Command Reference

**Minecraft Commands** (In-game):
- `/portalcord` or `/pc` - Displays plugin information, version, and help
- `/portalcord reload` - Reloads configuration file without server restart
  - Permission: `portalcord.reload`
  - Default: OP only

**Discord Slash Commands**:
All Discord commands require the bot to have `applications.commands` scope and appropriate channel permissions.

- `/list` - Shows online players, their ping, and total count in a rich embed
  - Access: Everyone (@everyone can use)
  - Cooldown: None
  
- `/server` - Displays server statistics including current TPS (Ticks Per Second), server version, uptime duration, and online player count vs max slots
  - Access: Everyone
  - Updates: Real-time data
  
- `/announce &lt;message: string&gt;` - Broadcasts a message to all online Minecraft players with special formatting (colored [Discord] Announcement prefix)
  - Access: Restricted to Role IDs listed in `commands.admin-roles`
  - Parameter: message (required, max 2000 characters)
  - Confirmation: Returns success/failure message to Discord
  
- `/execute &lt;command: string&gt;` - Executes any command as console from Discord
  - Access: Restricted to Role IDs listed in `console-access.allowed-roles`
  - Security: Disabled by default in config (console-access.enabled: false)
  - Logging: All executed commands logged with user attribution
  - Warning: Powerful feature - only grant to server owners/senior admins

---

### 🔐 Permission System Details

**Minecraft Permission Nodes**:
- `portalcord.admin` - Access to base /portalcord command (Default: OP)
- `portalcord.reload` - Permission to reload configuration (Default: OP)

**Discord Role Configuration**:
Discord permissions are not based on Minecraft ranks but on Discord Role IDs. Configure these in `plugins/PortalCord/config.yml`:

```yaml
commands:
  admin-roles:
    - "123456789012345678"  # Admin Role ID
    - "987654321098765432"  # Moderator Role ID
  
  console-access:
    enabled: false  # Must be true to use /execute
    allowed-roles:
      - "098765432109876543"  # Owner Role ID only
