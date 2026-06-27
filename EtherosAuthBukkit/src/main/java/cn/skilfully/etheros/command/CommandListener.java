package cn.skilfully.etheros.command;

import cn.skilfully.etheros.EtherosAuthBukkit;
import cn.skilfully.etheros.command.handler.*;
import cn.skilfully.etheros.config.ConfigManager;
import cn.skilfully.etheros.etherosframework.di.annotation.Autowired;
import cn.skilfully.etheros.etherosframework.di.annotation.PostConstruct;
import cn.skilfully.etheros.etherosframework.di.annotation.Service;
import cn.skilfully.etheros.utils.Messenger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@Service
public class CommandListener implements CommandExecutor {

    private static final List<String> usingCommands = Arrays.asList("etherosauth", "login", "register", "resetPassword");

    @Autowired
    private CommonCommandHandler commandHandler;

    @Autowired
    private LoginCommandHandler loginCommandHandler;

    @Autowired
    private RegisterCommandHandler registerCommandHandler;

    @Autowired
    private ResetPasswordCommandHandler resetPasswordCommandHandler;

    @Autowired
    private ConfigManager configManager;

    @PostConstruct
    private void register() {
        EtherosAuthBukkit plugin = EtherosAuthBukkit.getPlugin(EtherosAuthBukkit.class);
        for (String command : usingCommands) {
            PluginCommand pc = plugin.getCommand(command);
            if (pc != null) {
                pc.setExecutor(this);
            } else {
                Messenger.consoleError(configManager.getPluginLanguage().getError().getRegisterCommand());
                EtherosAuthBukkit.disable();
                return;
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (command.getName()) {
            case "etherosauth" -> commandHandler.handle(sender, args);
            case "login" -> loginCommandHandler.handle(sender, args);
            case "register" -> registerCommandHandler.handle(sender, args);
            case "resetPassword" -> resetPasswordCommandHandler.handle(sender, args);
        }
        return true;
    }

}
