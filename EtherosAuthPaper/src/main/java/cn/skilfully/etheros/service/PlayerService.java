package cn.skilfully.etheros.service;

import cn.skilfully.etheros.EtherosAuthPaper;
import cn.skilfully.etheros.config.ConfigManager;
import cn.skilfully.etheros.etherosframework.di.annotation.Autowired;
import cn.skilfully.etheros.etherosframework.di.annotation.PostConstruct;
import cn.skilfully.etheros.etherosframework.di.annotation.PreDestroy;
import cn.skilfully.etheros.etherosframework.di.annotation.Service;
import cn.skilfully.etheros.utils.ComponentTextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class PlayerService {

    @Autowired
    private ConfigManager configManager;

    private final Set<UUID> noLoginPlayers = new HashSet<>();
    private int reminderTaskId = -1;

    @PostConstruct
    private void init() {
        EtherosAuthPaper plugin = EtherosAuthPaper.getPlugin(EtherosAuthPaper.class);
        reminderTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin, this::sendLoginReminders, 100L, 160L
        ).getTaskId();
    }

    @PreDestroy
    private void preDestroy() {
        if (reminderTaskId != -1) {
            Bukkit.getScheduler().cancelTask(reminderTaskId);
        }
        noLoginPlayers.clear();
    }

    public void addNoLoginPlayer(UUID uuid) {
        noLoginPlayers.add(uuid);
    }

    public void removeNoLoginPlayer(UUID uuid) {
        noLoginPlayers.remove(uuid);
    }

    public boolean isNoLogin(UUID uuid) {
        return noLoginPlayers.contains(uuid);
    }

    private void sendLoginReminders() {
        String type = configManager.getPluginConfig().getAuthentication().getType();
        String message = switch (type) {
            case "local" -> configManager.getPluginLanguage().getLogin().getLocal().getMessage();
            case "official" -> configManager.getPluginLanguage().getLogin().getOfficial().getMessage();
            case "custom" -> configManager.getPluginLanguage().getLogin().getCustom().getMessage();
            default -> null;
        };
        if (message == null) return;

        var msgComponent = ComponentTextUtil.text(message);
        EtherosAuthPaper plugin = EtherosAuthPaper.getPlugin(EtherosAuthPaper.class);
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (isNoLogin(player.getUniqueId())) {
                    player.sendMessage(msgComponent);
                }
            }
        });
    }

}
