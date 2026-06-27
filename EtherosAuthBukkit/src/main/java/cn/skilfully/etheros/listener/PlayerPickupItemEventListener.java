package cn.skilfully.etheros.listener;

import cn.skilfully.etheros.EtherosAuthBukkit;
import cn.skilfully.etheros.etherosframework.di.annotation.Autowired;
import cn.skilfully.etheros.etherosframework.di.annotation.PostConstruct;
import cn.skilfully.etheros.etherosframework.di.annotation.Service;
import cn.skilfully.etheros.service.PlayerService;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.plugin.Plugin;

@Service
public class PlayerPickupItemEventListener implements Listener {

    @Autowired
    private PlayerService playerService;

    @PostConstruct
    private void init() {
        Plugin plugin = EtherosAuthBukkit.getPlugin(EtherosAuthBukkit.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player) {
            if (playerService.isNoLogin(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

}
