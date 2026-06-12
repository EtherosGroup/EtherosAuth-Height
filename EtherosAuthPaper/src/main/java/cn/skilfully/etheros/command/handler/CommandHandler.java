package cn.skilfully.etheros.command.handler;

import org.bukkit.command.CommandSender;

public interface CommandHandler {

    void handle(CommandSender sender, String[] args);

}
