package cn.skilfully.etheros.listener;

import cn.skilfully.etheros.EtherosAuthBukkit;
import cn.skilfully.etheros.etherosframework.di.annotation.Autowired;
import cn.skilfully.etheros.etherosframework.di.annotation.PostConstruct;
import cn.skilfully.etheros.etherosframework.di.annotation.Service;
import cn.skilfully.etheros.service.PlayerService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;

@Service
public class EntityDamageByEntityEventListener implements Listener {

    @Autowired
    private PlayerService playerService;

    @PostConstruct
    private void init() {
        Plugin plugin = EtherosAuthBukkit.getPlugin(EtherosAuthBukkit.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (playerService.isNoLogin(attacker.getUniqueId())) {
                event.setCancelled(true);
            }
        }
        else if (event.getEntity() instanceof Player damager) {
            if (playerService.isNoLogin(damager.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

}
