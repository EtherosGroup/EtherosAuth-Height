package cn.skilfully.etheros.command.handler;

import cn.skilfully.etheros.etherosframework.di.annotation.Service;
import cn.skilfully.etheros.etherosframework.di.annotation.Value;
import cn.skilfully.etheros.utils.Messenger;
import org.bukkit.command.CommandSender;

@Service
public class CommonCommandHandler implements CommandHandler {

    @Value("plugin.info")
    private String pluginInfo;

    @Override
    public void handle(CommandSender sender, String[] args) {
        Messenger.sendAutoInfo(sender, pluginInfo);
    }

}
