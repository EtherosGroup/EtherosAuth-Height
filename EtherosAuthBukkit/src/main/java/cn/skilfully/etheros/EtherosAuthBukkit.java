package cn.skilfully.etheros;

import cn.skilfully.etheros.etherosframework.di.core.ApplicationContext;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class EtherosAuthBukkit extends JavaPlugin {

    private ApplicationContext context;

    @Override
    public void onEnable() {
        context = ApplicationContext.run(EtherosAuthBukkit.class);
    }

    @Override
    public void onDisable() {
        if (context != null) {
            context.shutdown();
        }
    }

    public static void disable() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.disablePlugin(EtherosAuthBukkit.getPlugin(EtherosAuthBukkit.class));
    }

}
