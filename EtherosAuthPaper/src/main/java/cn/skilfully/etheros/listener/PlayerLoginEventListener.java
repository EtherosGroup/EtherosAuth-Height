package cn.skilfully.etheros.listener;

import cn.skilfully.etheros.EtherosAuthPaper;
import cn.skilfully.etheros.config.ConfigManager;
import cn.skilfully.etheros.database.PlayerLocationDAO;
import cn.skilfully.etheros.database.entity.PlayerLocationEntity;
import cn.skilfully.etheros.etherosframework.di.annotation.Autowired;
import cn.skilfully.etheros.etherosframework.di.annotation.PostConstruct;
import cn.skilfully.etheros.etherosframework.di.annotation.Service;
import cn.skilfully.etheros.service.*;
import cn.skilfully.etheros.utils.ComponentTextUtil;
import cn.skilfully.etheros.utils.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.function.Function;

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

    @Autowired
    private PlayerLocationDAO playerLocationDAO;

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

        if (configManager.getPluginConfig().getAuthentication().getAction().getLocate().getAutoReturn()) {
            localAuthService.tpToLoginLocate(newPlayer);
        }

        playerService.addNoLoginPlayer(newPlayer.getUniqueId());
        localAuthService.runMessageTask(newPlayer.getUniqueId());

        event.allow();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        localAuthService.removeMessageTask(player.getUniqueId());
        if (configManager.getPluginConfig().getAuthentication().getAction().getLocate().getAutoReturn()) {
            Location location = player.getLocation();
            PlayerLocationEntity locationEntity = new PlayerLocationEntity();
            locationEntity
                    .setUuid(player.getUniqueId())
                    .setX(location.getX())
                    .setY(location.getY())
                    .setZ(location.getZ())
                    .setYaw(location.getYaw())
                    .setPitch(location.getPitch())
                    .setWorld(location.getWorld().getName());
            if (playerLocationDAO.existsByUuid(locationEntity.getUuid())) {
                playerLocationDAO.update(locationEntity);
            } else {
                playerLocationDAO.create(locationEntity);
            }
        }
    }

}