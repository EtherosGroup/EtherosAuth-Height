package cn.skilfully.etheros.command.handler;

import cn.skilfully.etheros.EtherosAuthPaper;
import cn.skilfully.etheros.config.ConfigManager;
import cn.skilfully.etheros.etherosframework.di.annotation.Autowired;
import cn.skilfully.etheros.etherosframework.di.annotation.PostConstruct;
import cn.skilfully.etheros.etherosframework.di.annotation.Service;
import cn.skilfully.etheros.service.LocalAuthService;
import cn.skilfully.etheros.utils.ComponentTextUtil;
import cn.skilfully.etheros.utils.Messenger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.function.Consumer;

@Service
public class LoginCommandHandler implements CommandHandler {

    @Autowired
    private ConfigManager configManager;

    @Autowired
    private LocalAuthService localAuthService;

    private Consumer<Request> loginHandler;

    @PostConstruct
    private void init() {
        String type = configManager.getPluginConfig().getAuthentication().getType();
        loginHandler = switch (type) {
            case "local" -> this::handleModeLocal;
            case "official" -> this::handleModeOfficial;
            case "custom" -> this::handleModeCustom;
            default -> {
                Messenger.consoleError(configManager.getPluginLanguage().getError().getUnknownAuthType(), type);
                EtherosAuthPaper.disable();
                yield null;
            }
        };
    }

    @Override
    public void handle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            Messenger.consoleError(configManager.getPluginLanguage().getCommand().getPlayerOnly());
            return;
        }
        loginHandler.accept(new Request(sender, args));
    }

    private void handleModeLocal(Request request) {
        Player player = (Player) request.sender;
        if (request.args.length < 1) {
            Messenger.sendMessageToPlayer(player, configManager.getPluginLanguage().getCommand().getIncorrect());
            return;
        }
        String message = switch (localAuthService.login(player.getUniqueId(), request.args[0])) {
            case OK -> {
                cleanTitle(player);
                yield  configManager.getPluginLanguage().getCommand().getLogin().getOk();
            }
            case NO_ACCOUNT ->  configManager.getPluginLanguage().getCommand().getLogin().getNoAccount();
            case WRONG_PASSWORD ->  configManager.getPluginLanguage().getCommand().getLogin().getWrongPassword();
        };
        Messenger.sendMessageToPlayer(player, message);
    }

    private void handleModeOfficial(Request request) {
    }

    private void handleModeCustom(Request request) {
    }

    private record Request(CommandSender sender, String[] args) {}

    private void cleanTitle(Player player) {
        Component title = ComponentTextUtil.text("");
        Component subtitle = ComponentTextUtil.text("");
        Title.Times times = Title.Times.of(
                Duration.ofMillis(0),
                Duration.ofSeconds(0),
                Duration.ofMillis(0)
        );
        player.showTitle(Title.title(title, subtitle, times));
    }

    private void tpPlayer(Player player) {
    }
}
