package cn.skilfully.etheros.hook;

import cn.skilfully.etheros.etherosframework.di.annotation.Autowired;
import cn.skilfully.etheros.etherosframework.di.annotation.Service;
import cn.skilfully.etheros.etherosframework.hook.core.HookManager;
import cn.skilfully.etheros.etherosframework.hook.entity.HookEvent;
import org.bukkit.entity.Player;

import java.util.Map;

@Service
public class PlayerHook {

    @Autowired
    private HookManager hookManager;

    public void onPlayerLogin(Player player) {
        HookEvent hookEvent = new HookEvent();
        hookEvent
                .setData(Map.of("player-uuid", player.getUniqueId().toString()));
        hookManager.callEvent("ehteorsauth:playerLogin", hookEvent);
    }

}
