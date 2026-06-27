package cn.skilfully.etheros.command.handler;

import cn.skilfully.etheros.EtherosAuthBukkit;
import cn.skilfully.etheros.config.ConfigManager;
import cn.skilfully.etheros.etherosframework.di.annotation.Autowired;
import cn.skilfully.etheros.etherosframework.di.annotation.PostConstruct;
import cn.skilfully.etheros.etherosframework.di.annotation.Service;
import cn.skilfully.etheros.service.LocalAuthService;
import cn.skilfully.etheros.utils.Messenger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

@Service
public class ResetPasswordCommandHandler implements CommandHandler {

    @Autowired
    private ConfigManager configManager;

    @Autowired
    private LocalAuthService localAuthService;

    private Consumer<Request> modeHandler;

    @PostConstruct
    private void init() {
        String type = configManager.getPluginConfig().getAuthentication().getType();
        modeHandler = switch (type) {
            case "local" -> this::handleModeLocal;
            case "official" -> this::handleModeOfficial;
            case "custom" -> this::handleModeCustom;
            default -> {
                Messenger.consoleError(configManager.getPluginLanguage().getError().getUnknownAuthType(), type);
                EtherosAuthBukkit.disable();
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
        modeHandler.accept(new Request(sender, args));
    }

    private void handleModeLocal(Request request) {
        Player player = (Player) request.sender;
        if (request.args.length < 2) {
            Messenger.sendMessageToPlayer(player, configManager.getPluginLanguage().getCommand().getIncorrect());
            return;
        }
        var result = localAuthService.resetPassword(player.getUniqueId(), request.args[0], request.args[1]);
        String message = switch (result) {
            case OK -> configManager.getPluginLanguage().getCommand().getResetPassword().getOk();
            case NO_ACCOUNT -> configManager.getPluginLanguage().getCommand().getResetPassword().getNoAccount();
            case WRONG_OLD_PASSWORD -> configManager.getPluginLanguage().getCommand().getResetPassword().getWrongOldPassword();
            case SAME_PASSWORD -> configManager.getPluginLanguage().getCommand().getResetPassword().getSamePassword();
            case INVALID_PASSWORD -> configManager.getPluginLanguage().getCommand().getResetPassword().getInvalidPassword();
        };
        Messenger.sendMessageToPlayer(player, message);
    }

    private void handleModeOfficial(Request request) {

    }

    private void handleModeCustom(Request request) {

    }

    private record Request(CommandSender sender, String[] args) {}
}
