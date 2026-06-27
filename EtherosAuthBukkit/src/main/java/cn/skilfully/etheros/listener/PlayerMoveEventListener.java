package cn.skilfully.etheros.listener;

import cn.skilfully.etheros.EtherosAuthBukkit;
import cn.skilfully.etheros.etherosframework.di.annotation.Autowired;
import cn.skilfully.etheros.etherosframework.di.annotation.PostConstruct;
import cn.skilfully.etheros.etherosframework.di.annotation.Service;
import cn.skilfully.etheros.service.PlayerService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@Service
public class PlayerMoveEventListener implements Listener {

    @Autowired
    private PlayerService playerService;

    @PostConstruct
    private void init() {
        EtherosAuthBukkit plugin = EtherosAuthBukkit.getPlugin(EtherosAuthBukkit.class);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!playerService.isNoLogin(player.getUniqueId())) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlockX() == to.getBlockX()
                && from.getBlockZ() == to.getBlockZ()
                && Math.abs(from.getY() - to.getY()) < 0.001D) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerService.removeNoLoginPlayer(event.getPlayer().getUniqueId());
    }
}
