package cn.skilfully.etheros.service;

import cn.skilfully.etheros.EtherosAuthPaper;
import cn.skilfully.etheros.config.ConfigManager;
import cn.skilfully.etheros.database.AuthDAO;
import cn.skilfully.etheros.database.PlayerLocationDAO;
import cn.skilfully.etheros.database.entity.PlayerAccountEntity;
import cn.skilfully.etheros.database.entity.PlayerLocationEntity;
import cn.skilfully.etheros.etherosframework.di.annotation.Autowired;
import cn.skilfully.etheros.etherosframework.di.annotation.PostConstruct;
import cn.skilfully.etheros.etherosframework.di.annotation.PreDestroy;
import cn.skilfully.etheros.etherosframework.di.annotation.Service;
import cn.skilfully.etheros.utils.ComponentTextUtil;
import cn.skilfully.etheros.utils.Messenger;
import cn.skilfully.etheros.utils.PasswordUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class LocalAuthService {

    @Autowired
    private AuthDAO authDAO;

    @Autowired
    private PlayerLocationDAO playerLocationDAO;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private ConfigManager configManager;

    private static final EtherosAuthPaper plugin = EtherosAuthPaper.getPlugin(EtherosAuthPaper.class);

    private Location loginLocation = null;

    @PostConstruct
    private void init() {
        String type = configManager.getPluginConfig().getAuthentication().getType();
        getTitle = switch (type) {
            case "local" -> () -> configManager.getPluginLanguage().getLogin().getLocal().getTitle();
            case "official" -> () -> configManager.getPluginLanguage().getLogin().getOfficial().getTitle();
            case "custom" -> () -> configManager.getPluginLanguage().getLogin().getCustom().getTitle();
            default -> {
                Messenger.consoleError(configManager.getPluginLanguage().getError().getUnknownAuthType(), type);
                EtherosAuthPaper.disable();
                yield null;
            }
        };
        getSubTitle = switch (type) {
            case "local" -> () -> configManager.getPluginLanguage().getLogin().getLocal().getSubtitle();
            case "official" -> () -> configManager.getPluginLanguage().getLogin().getOfficial().getSubtitle();
            case "custom" -> () -> configManager.getPluginLanguage().getLogin().getCustom().getSubtitle();
            default -> {
                Messenger.consoleError(configManager.getPluginLanguage().getError().getUnknownAuthType(), type);
                EtherosAuthPaper.disable();
                yield null;
            }
        };
        titleTaskId = Bukkit.getScheduler().runTaskTimer(plugin, this::sendLoginTitles, 20L, 20L).getTaskId();
        String loginWorldName = configManager.getPluginConfig().getAuthentication().getAction().getLocate().getWorld();
        World loginWorld = Bukkit.getWorld(loginWorldName);
        if (loginWorld == null) {
            Messenger.consoleError(configManager.getPluginLanguage().getError().getUnknownWorld(), loginWorldName);
            EtherosAuthPaper.disable();
            return;
        }
        loginLocation = new Location(
                loginWorld,
                configManager.getPluginConfig().getAuthentication().getAction().getLocate().getX(),
                configManager.getPluginConfig().getAuthentication().getAction().getLocate().getY(),
                configManager.getPluginConfig().getAuthentication().getAction().getLocate().getZ(),
                configManager.getPluginConfig().getAuthentication().getAction().getLocate().getYaw(),
                configManager.getPluginConfig().getAuthentication().getAction().getLocate().getPitch()
        );
    }

    @PreDestroy
    private void destroy() {
        if (titleTaskId != -1) {
            Bukkit.getScheduler().cancelTask(titleTaskId);
        }
    }

    public void reload(CommandSender sender) {
        if (sender == null) {
            sender = Bukkit.getConsoleSender();
        }
        String loginWorldName = getConfigWorld();
        World loginWorld = Bukkit.getWorld(loginWorldName);
        if (loginWorld == null) {
            Messenger.sendAutoError(sender, configManager.getPluginLanguage().getFailed().getReload(), "LocalAuthService", "未知世界名称 " + loginWorldName);
        } else {
            loginLocation = new Location(
                    loginWorld,
                    configManager.getPluginConfig().getAuthentication().getAction().getLocate().getX(),
                    configManager.getPluginConfig().getAuthentication().getAction().getLocate().getY(),
                    configManager.getPluginConfig().getAuthentication().getAction().getLocate().getZ(),
                    configManager.getPluginConfig().getAuthentication().getAction().getLocate().getYaw(),
                    configManager.getPluginConfig().getAuthentication().getAction().getLocate().getPitch()
            );
        }
    }

    public LoginResult login(UUID uuid, String password) {
        if (uuid == null || password == null) return LoginResult.NO_ACCOUNT;
        Optional<PlayerAccountEntity> optionalPlayerAccountEntity = authDAO.findByUuid(uuid);
        if (optionalPlayerAccountEntity.isEmpty()) return LoginResult.NO_ACCOUNT;
        PlayerAccountEntity playerAccountEntity = optionalPlayerAccountEntity.get();
        if (PasswordUtil.verify(password, playerAccountEntity.getPassword())) {
            playerService.removeNoLoginPlayer(uuid);
            if (configManager.getPluginConfig().getAuthentication().getAction().getLocate().getAutoReturn()) {
                tpReturn(uuid);
            }
            return LoginResult.OK;
        }
        return LoginResult.WRONG_PASSWORD;
    }

    public RegisterResult register(UUID uuid, String name, String password) {
        if (uuid == null || password == null) return RegisterResult.INVALID_NAME;
        if (!checkPassword(password)) return RegisterResult.INVALID_PASSWORD;
        if (!checkName(name)) return RegisterResult.INVALID_NAME;
        if (authDAO.existsByUuid(uuid)) {
            return RegisterResult.ACCOUNT_EXISTS;
        }
        PlayerAccountEntity playerAccountEntity = new PlayerAccountEntity();
        playerAccountEntity
                .setUuid(uuid)
                .setPassword(PasswordUtil.hash(password));
        authDAO.create(playerAccountEntity);
        return RegisterResult.OK;
    }

    public ResetPasswordResult resetPassword(UUID uuid, String oldPassword, String newPassword) {
        if (uuid == null || oldPassword == null || newPassword == null) return ResetPasswordResult.NO_ACCOUNT;
        Optional<PlayerAccountEntity> optionalPlayerAccountEntity = authDAO.findByUuid(uuid);
        if (optionalPlayerAccountEntity.isEmpty()) return ResetPasswordResult.NO_ACCOUNT;
        PlayerAccountEntity playerAccountEntity = optionalPlayerAccountEntity.get();
        if (!PasswordUtil.verify(oldPassword, playerAccountEntity.getPassword())) return ResetPasswordResult.WRONG_OLD_PASSWORD;
        if (oldPassword.equals(newPassword)) return ResetPasswordResult.SAME_PASSWORD;
        if (!checkPassword(newPassword)) return ResetPasswordResult.INVALID_PASSWORD;
        playerAccountEntity.setPassword(PasswordUtil.hash(newPassword));
        authDAO.update(playerAccountEntity);
        playerService.addNoLoginPlayer(uuid);
        runMessageTask(uuid);
        return ResetPasswordResult.OK;
    }

    public void delete(UUID uuid, String password) {

    }

    public void adminDelete(UUID uuid) {

    }

    public void tpToLoginLocate(Player player) {
        player.teleportAsync(loginLocation);
    }

    private void tpReturn(UUID uuid) {
        var oeLocation = playerLocationDAO.findByUuid(uuid);
        if (oeLocation.isEmpty()) {
            return;
        }
        var eLocation = oeLocation.get();
        World world = Bukkit.getWorld(eLocation.getWorld());
        if (world == null) {
            Messenger.consoleError(configManager.getPluginLanguage().getFailed().getTp(), "未知世界：" + eLocation.getWorld());
            return;
        }
        Location target = new Location(
                world,
                eLocation.getX(),
                eLocation.getY(),
                eLocation.getZ(),
                eLocation.getYaw(),
                eLocation.getPitch()
        );
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            Messenger.consoleError(configManager.getPluginLanguage().getFailed().getTp(), "未知玩家（UUID）：" + uuid);
            return;
        }
        player.teleportAsync(target);
    }

    private boolean checkPassword(String password) {
        String regex = configManager.getPluginConfig().getAuthentication().getRegister().getRestrictions().getRegular().getPassword();
        return password != null && password.matches(regex);
    }

    private boolean checkName(String name) {
        String regex = configManager.getPluginConfig().getAuthentication().getRegister().getRestrictions().getRegular().getName();
        return name != null && name.matches(regex);
    }

    //

    private Supplier<String> getTitle;
    private Supplier<String> getSubTitle;

    private final Map<UUID, Long> loginTimestamps = new HashMap<>();
    private final Map<UUID, BukkitTask> kickTasks = new HashMap<>();
    private int titleTaskId = -1;

    public void runMessageTask(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            return;
        }
        if (!playerService.isNoLogin(uuid)) {
            return;
        }

        loginTimestamps.putIfAbsent(uuid, System.currentTimeMillis());

        int totalTimeoutSeconds = configManager.getPluginConfig().getAuthentication().getLoginTimeout();
        Long loginTime = loginTimestamps.get(uuid);
        int remaining = totalTimeoutSeconds;
        if (loginTime != null) {
            remaining = totalTimeoutSeconds - (int) ((System.currentTimeMillis() - loginTime) / 1000);
            if (remaining < 0) remaining = 0;
        }
        showTitle(player, remaining);

        BukkitTask existingTask = kickTasks.remove(uuid);
        if (existingTask != null) {
            existingTask.cancel();
        }

        BukkitTask kickTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (playerService.isNoLogin(uuid)) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    p.kick(ComponentTextUtil.text(configManager.getPluginLanguage().getRejectMessage().getLoginTimeout()));
                }
                playerService.removeNoLoginPlayer(uuid);
            }
            loginTimestamps.remove(uuid);
            kickTasks.remove(uuid);
        }, totalTimeoutSeconds * 20L);
        kickTasks.put(uuid, kickTask);
    }

    public void removeMessageTask(UUID uuid) {
        BukkitTask kickTask = kickTasks.remove(uuid);
        if (kickTask != null) {
            kickTask.cancel();
        }
        loginTimestamps.remove(uuid);
        playerService.removeNoLoginPlayer(uuid);
    }

    private void showTitle(Player player, int timeoutSeconds) {
        Component title = ComponentTextUtil.text(getTitle.get());
        Component subtitle = ComponentTextUtil.text(Messenger.formatMessageArgs(getSubTitle.get(), timeoutSeconds));
        Title.Times times = Title.Times.of(
                Duration.ofMillis(0),
                Duration.ofSeconds(3),
                Duration.ofMillis(0)
        );
        player.showTitle(Title.title(title, subtitle, times));
    }

    private void sendLoginTitles() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (playerService.isNoLogin(uuid)) {
                runMessageTask(uuid);
            } else {
                loginTimestamps.remove(uuid);
            }
        }
    }

    public enum LoginResult {
        OK, NO_ACCOUNT, WRONG_PASSWORD
    }

    public enum RegisterResult {
        OK, INVALID_NAME, ACCOUNT_EXISTS, INVALID_PASSWORD
    }

    public enum ResetPasswordResult {
        OK, NO_ACCOUNT, WRONG_OLD_PASSWORD, SAME_PASSWORD, INVALID_PASSWORD
    }

    // utils
    private String getConfigWorld() {
        return configManager.getPluginConfig().getAuthentication().getAction().getLocate().getWorld();
    }

}
