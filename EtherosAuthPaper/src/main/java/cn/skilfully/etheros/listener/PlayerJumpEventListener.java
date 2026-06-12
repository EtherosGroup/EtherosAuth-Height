package cn.skilfully.etheros.listener;

import cn.skilfully.etheros.EtherosAuthPaper;
import cn.skilfully.etheros.etherosframework.di.annotation.Autowired;
import cn.skilfully.etheros.etherosframework.di.annotation.PostConstruct;
import cn.skilfully.etheros.etherosframework.di.annotation.Service;
import cn.skilfully.etheros.service.PlayerService;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

@Service
public class PlayerJumpEventListener implements Listener {

    @Autowired
    private PlayerService playerService;

    @PostConstruct
    private void init() {
        Plugin plugin = EtherosAuthPaper.getPlugin(EtherosAuthPaper.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJump(PlayerJumpEvent event) {
        Player player = event.getPlayer();
        if (playerService.isNoLogin(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

}
