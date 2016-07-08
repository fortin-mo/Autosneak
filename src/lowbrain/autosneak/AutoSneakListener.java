package lowbrain.autosneak;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;

public class AutoSneakListener
implements Listener {
    private final AutoSneak plugin = AutoSneak.Instance;

    public AutoSneakListener(AutoSneak plugin) {
        Bukkit.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().hasPermission("autosneak.auto")) {
            AutoSneak.sneakingPlayers.add(event.getPlayer().getUniqueId());
            event.getPlayer().setDisplayName(ChatColor.WHITE + "");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (AutoSneak.sneakingPlayers.contains(event.getPlayer().getUniqueId())) {
            AutoSneak.sneakingPlayers.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!AutoSneak.sneakingPlayers.isEmpty() && AutoSneak.sneakingPlayers.contains(event.getPlayer().getUniqueId())) {
            AutoSneak.sneakingPlayers.remove(event.getPlayer().getUniqueId());
            event.getPlayer().setDisplayName(event.getPlayer().getName());
        }
    }
}

