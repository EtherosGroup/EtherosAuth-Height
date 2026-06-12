package cn.skilfully.etheros.listener;

import cn.skilfully.etheros.EtherosAuthPaper;
import cn.skilfully.etheros.config.ConfigManager;
import cn.skilfully.etheros.etherosframework.di.annotation.Autowired;
import cn.skilfully.etheros.etherosframework.di.annotation.PostConstruct;
import cn.skilfully.etheros.etherosframework.di.annotation.PreDestroy;
import cn.skilfully.etheros.etherosframework.di.annotation.Service;
import cn.skilfully.etheros.service.*;
import cn.skilfully.etheros.utils.ComponentTextUtil;
import cn.skilfully.etheros.utils.Messenger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class PlayerLoginEventListener implements Listener {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private ServerService serverService;

    @Autowired
    private ConfigManager configManager;

    @Autowired
    private CustomAuthService customAuthService;

    @Autowired
    private OfficialAuthService officialAuthService;

    @Autowired
    private LocalAuthService localAuthService;

    private Function<UUID, Boolean> checkBanned;

    @PostConstruct
    private void init() {
        EtherosAuthPaper plugin = EtherosAuthPaper.getPlugin(EtherosAuthPaper.class);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        String type = configManager.getPluginConfig().getAuthentication().getType();
        checkBanned = switch (type) {
            case "local" -> (ignore) -> false;
            case "official" -> (uuid) -> officialAuthService.isBanned(uuid);
            case "custom" -> (uuid) -> customAuthService.isBanned(uuid);
            default -> {
                Messenger.consoleError(configManager.getPluginLanguage().getError().getUnknownAuthType(), type);
                EtherosAuthPaper.disable();
                yield null;
            }
        };
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();

        if (checkBanned == null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    ComponentTextUtil.text(configManager.getPluginLanguage().getRejectMessage().getInternalError()));
            return;
        }
        if (checkBanned.apply(uuid)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                    ComponentTextUtil.text(configManager.getPluginLanguage().getRejectMessage().getBannedLocal()));
            return;
        }
        if (officialAuthService.isGlobalBanned(uuid)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                    ComponentTextUtil.text(configManager.getPluginLanguage().getRejectMessage().getBannedGlobal()));
            return;
        }

        Player existingPlayer = Bukkit.getPlayer(uuid);
        if (existingPlayer != null && existingPlayer.isOnline()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    ComponentTextUtil.text(configManager.getPluginLanguage().getRejectMessage().getLoggedIn()));
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player newPlayer = event.getPlayer();
        if (serverService.isLoading()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ComponentTextUtil.text(configManager.getPluginLanguage().getRejectMessage().getLoading()));
            return;
        }
        if (serverService.isUpdating()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ComponentTextUtil.text(configManager.getPluginLanguage().getRejectMessage().getUpdating()));
            return;
        }

        playerService.addNoLoginPlayer(newPlayer.getUniqueId());
        localAuthService.runMessageTask(newPlayer.getUniqueId());

        Location location = newPlayer.getLocation();
        //TODO 如果auto-return = true则记录当前位置到数据库，然后tp到locate中的位置


        event.allow();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        localAuthService.removeMessageTask(event.getPlayer().getUniqueId());
    }

}