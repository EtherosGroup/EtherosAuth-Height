package cn.skilfully.etheros.command.handler;

import cn.skilfully.etheros.EtherosAuthBukkit;
import cn.skilfully.etheros.config.ConfigManager;
import cn.skilfully.etheros.etherosframework.di.annotation.Autowired;
import cn.skilfully.etheros.etherosframework.di.annotation.PostConstruct;
import cn.skilfully.etheros.etherosframework.di.annotation.Service;
import cn.skilfully.etheros.service.LocalAuthService;
import cn.skilfully.etheros.utils.Messenger;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

@Service
public class RegisterCommandHandler implements CommandHandler {

    @Autowired
    private ConfigManager configManager;

    @Autowired
    private LocalAuthService localAuthService;

    private Consumer<Request> registerHandler;

    @PostConstruct
    private void init() {
        String type = configManager.getPluginConfig().getAuthentication().getType();
        registerHandler = switch (type) {
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
        if (sender instanceof ConsoleCommandSender) {
            Messenger.consoleError(configManager.getPluginLanguage().getCommand().getPlayerOnly());
            return;
        }
        if (!configManager.getPluginConfig().getAuthentication().getRegister().getEnabled()) {
            Player player = (Player) sender;
            Messenger.sendMessageToPlayer(player, configManager.getPluginLanguage().getCommand().getRegister().getNoEnabled());
            return;
        }
        registerHandler.accept(new Request(sender, args));
    }

    private void handleModeLocal(Request request) {
        Player player = (Player) request.sender;
        if (request.args.length < 2) {
            Messenger.sendMessageToPlayer(player, configManager.getPluginLanguage().getCommand().getIncorrect());
            return;
        }
        if (!request.args[0].equals(request.args[1])) {
            Messenger.sendMessageToPlayer(player, configManager.getPluginLanguage().getCommand().getRegister().getPasswordMismatch());
            return;
        }

        String message = switch (localAuthService.register(player.getUniqueId(), player.getName(), request.args[1])) {
            case OK -> configManager.getPluginLanguage().getCommand().getRegister().getOk();
            case ACCOUNT_EXISTS -> configManager.getPluginLanguage().getCommand().getRegister().getAccountExists();
            case INVALID_NAME -> configManager.getPluginLanguage().getCommand().getRegister().getInvalidName();
            case INVALID_PASSWORD -> configManager.getPluginLanguage().getCommand().getRegister().getInvalidPassword();
        };
        Messenger.sendMessageToPlayer(player, message);
    }

    private void handleModeOfficial(Request request) {}

    private void handleModeCustom(Request request) {}

    private record Request(CommandSender sender, String[] args) {}
}
