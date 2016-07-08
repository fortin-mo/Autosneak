package lowbrain.autosneak;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoSneak
extends JavaPlugin {
    static ArrayList<UUID> sneakingPlayers = new ArrayList<UUID>();
    private HashMap<UUID, Long> cooldownTimes = new HashMap<UUID, Long>();
    public static AutoSneak Instance;
    private PluginDescriptionFile pdfFile;
    private String name;
    private String version;
    private static final Logger log;
    private Configuration config;
    private String sneakOnMessage;
    private String sneakOffMessage;
    private String sneakCooldownMessage;
    private String sneakGiveMessage;
    private int sneakDuration;
    private int sneakCooldown;
    public static final Boolean debugging;

    static {
        log = Logger.getLogger("Minecraft");
        debugging = false;
    }

    public void onEnable() {
        this.config = this.getConfig();
        this.pdfFile = this.getDescription();
        this.name = this.pdfFile.getName();
        this.version = this.pdfFile.getVersion();
        AutoSneakListener listener = new AutoSneakListener(this);
        this.config.addDefault("messages.sneakOn", (Object)"&7You are now sneaking.".replace("&", "\u00a7"));
        this.config.addDefault("messages.sneakOff", (Object)"&7You are no longer sneaking.".replace("&", "\u00a7"));
        this.config.addDefault("messages.sneakGive", (Object)"&7Player <player> is now sneaking.".replace("&", "\u00a7"));
        this.config.addDefault("messages.sneakCooldown", (Object)"&4You must wait <time> seconds before you may sneak again.".replace("&", "\u00a7"));
        this.config.addDefault("options.timers.duration", (Object)0);
        this.config.addDefault("options.timers.cooldown", (Object)0);
        this.config.addDefault("options.timers.refresh", (Object)5);
        this.config.options().copyDefaults(true);
        this.sneakOnMessage = this.config.getString("messages.sneakOn").replace("&", "\u00a7");
        this.sneakOffMessage = this.config.getString("messages.sneakOff").replace("&", "\u00a7");
        this.sneakCooldownMessage = this.config.getString("messages.sneakCooldown").replace("&", "\u00a7");
        this.sneakGiveMessage = this.config.getString("messages.sneakGive").replace("&", "\u00a7");
        this.sneakDuration = this.config.getInt("options.timers.duration", 0);
        this.sneakCooldown = this.config.getInt("options.timers.cooldown", 0);
        this.saveConfig();
        this.setupAutosneak();
        String strEnable = "[" + this.name + "] " + this.version + " enabled.";
        log.info(strEnable);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("autosneak.sneak") && !player.isOp()) {
            return true;
        }
        if (args.length == 0) {
            this.toggleSneak(player);
            return true;
        } else {
            if (args.length != 1) return false;
            if (args[0].equalsIgnoreCase("on")) {
                this.setSneak(player, true);
                return true;
            } else {
                if (!args[0].equalsIgnoreCase("off")) return false;
                this.setSneak(player, false);
            }
        }
        return true;
    }

    private void setupAutosneak() {     
        for(Player p : this.getServer().getOnlinePlayers()){
        	if (p.hasPermission("autosneak.auto")) {
                this.setSneak(p, true);
            }
        }
    }

    private void toggleSneak(Player player) {
        if (sneakingPlayers.contains(player.getUniqueId())) {
            this.setSneak(player, false);
        } else {
            this.setSneak(player, true);
        }
    }

    private void setSneak(Player player, boolean sneak) {
        if (sneak) {
            if (this.sneakCooldown > 0 && this.cooldownTimes.containsKey(player.getUniqueId()) && this.cooldownTimes.get(player.getName()) > System.currentTimeMillis()) {
                player.sendMessage(this.sneakCooldownMessage.replaceAll("<time>", Integer.toString((int)Math.ceil((this.cooldownTimes.get(player.getName()) - System.currentTimeMillis()) / 1000))));
                return;
            }
            if (!player.hasPermission("autosneak.exempt")) {
                if (this.sneakCooldown > 0) {
                    this.cooldownTimes.put(player.getUniqueId(), System.currentTimeMillis() + (long)this.sneakCooldown * 1000);
                }
                if (this.sneakDuration > 0) {
                    this.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this, (Runnable)new SneakCooldown(player), (long)this.sneakDuration * 20);
                }
            }
            if (!sneakingPlayers.contains(player.getUniqueId())) {
                sneakingPlayers.add(player.getUniqueId());
            }
            player.setDisplayName(ChatColor.WHITE + "");
            player.sendMessage(this.sneakOnMessage);
        } else {
            player.sendMessage(this.sneakOffMessage);
            if (sneakingPlayers.contains(player.getUniqueId())) {
                sneakingPlayers.remove(player.getUniqueId());
            }
            player.setDisplayName(player.getName());
        }
    }

    public static void debug(String message) {
        if (debugging.booleanValue()) {
            log.info(message);
        }
    }

    public void onDisable() {
        String strDisable = "[" + this.name + "] " + this.version + " disabled.";
        log.info(strDisable);
    }

    public class SneakCooldown
    implements Runnable {
        private Player player;

        public SneakCooldown(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            if (this.player.isSneaking()) {
                AutoSneak.this.setSneak(this.player, false);
            }
        }
    }

}

