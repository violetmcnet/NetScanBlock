package net.violetmc.netscanblock;

import com.destroystokyo.paper.event.player.PlayerHandshakeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class PaperNetScanBlock extends JavaPlugin implements Listener {

    private List<String> allowed;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        allowed = getConfig().getStringList("allowed-domains");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerHandshake(PlayerHandshakeEvent event) {
        String hostname = event.getServerHostname();

        if (hostname == null || !allowed.contains(hostname.toLowerCase())) {
            event.setCancelled(true);
        }
    }

}
