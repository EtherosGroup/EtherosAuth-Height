package cn.skilfully.etheros.listener;

import cn.skilfully.etheros.EtherosAuthBukkit;
import cn.skilfully.etheros.config.ConfigManager;
import cn.skilfully.etheros.etherosframework.di.annotation.Autowired;
import cn.skilfully.etheros.etherosframework.di.annotation.PostConstruct;
import cn.skilfully.etheros.etherosframework.di.annotation.Service;
import cn.skilfully.etheros.service.PlayerService;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

@Service
public class PlayerCommandPreprocessEventListener implements Listener {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private ConfigManager configManager;

    @PostConstruct
    private void init() {
        Plugin plugin = EtherosAuthBukkit.getPlugin(EtherosAuthBukkit.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player instanceof ConsoleCommandSender) {
            return;
        }
        if (playerService.isNoLogin(player.getUniqueId())) {
            String command = event.getMessage().toLowerCase();
            for (String pass : configManager.getCommandWhitelist()) {
                if (command.startsWith(pass)) {
                    return;
                }
            }
            event.setCancelled(true);
        }
    }

}
