package cn.skilfully.etheros.hook;

import cn.skilfully.etheros.config.ConfigManager;
import cn.skilfully.etheros.etherosframework.di.annotation.Autowired;
import cn.skilfully.etheros.etherosframework.di.annotation.PostConstruct;
import cn.skilfully.etheros.etherosframework.di.annotation.Service;
import cn.skilfully.etheros.etherosframework.hook.core.HookManager;
import cn.skilfully.etheros.etherosframework.hook.entity.HookEvent;
import cn.skilfully.etheros.etherosframework.hook.entity.Priority;
import cn.skilfully.etheros.service.LocalAuthService;
import org.bukkit.command.CommandSender;

@Service
public class ReloadHook {

    @Autowired
    private HookManager hookManager;

    @Autowired
    private ConfigManager configManager;

    // 被管理的类
    @Autowired
    private LocalAuthService localAuthService;

    @PostConstruct
    private void init() {
        hookManager.register("etherosauth:_reload", this::onPluginReload, Priority.HIGH);
    }

    private void onPluginReload(HookEvent event) {
        Object oSender = event.getData().get("CommandSender");
        if (oSender instanceof CommandSender sender) {
            localAuthService.reload(sender);
        }
    }

}
