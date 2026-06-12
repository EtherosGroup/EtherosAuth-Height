package cn.skilfully.etheros.utils;

import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Messenger {

    private static final String consolePrefix = "&7| &bEtherosAuth &7|";

    @Setter
    private static String inGamePrefix = "";

    private final static String logInfo = "&7| &fINFO  &7| &r";
    private final static String logWarn = "&7| &eWARN  &7| &e";
    private final static String logError = "&7| &cERROR &7| &c";
    private final static String logDebug = "&7| &5DEBUG &7| &5";

    private static void sendMessage(CommandSender to, String message) {
        if (message == null || message.isEmpty()) {
            to.sendMessage("");
            return;
        }
        to.sendMessage(formatMessageColor(message));
    }

    private static void sendMessage(CommandSender to, String message, Object... args) {
        sendMessage(to, formatMessageColor(formatMessageArgs(message, args)));
    }

    public static void sendAutoInfo(CommandSender to, String message) {
        if (to instanceof Player) {
            sendMessageToPlayer((Player) to, message);
        } else if (to instanceof ConsoleCommandSender) {
            consoleInfo(message);
        } else {
            sendMessage(to, message);
        }
    }

    public static void sendAutoInfo(CommandSender to, String message, Object... args) {
        if (to instanceof Player) {
            sendMessageToPlayer((Player) to, message, args);
        } else if (to instanceof ConsoleCommandSender) {
            consoleInfo(message, args);
        } else {
            sendMessage(to, message, args);
        }
    }

    public static void sendAutoWarn(CommandSender to, String message) {
        if (to instanceof Player) {
            sendMessageToPlayer((Player) to, message);
        } else if (to instanceof ConsoleCommandSender) {
            consoleWarn(message);
        } else {
            sendMessage(to, message);
        }
    }

    public static void sendAutoWarn(CommandSender to, String message, Object... args) {
        if (to instanceof Player) {
            sendMessageToPlayer((Player) to, message, args);
        } else if (to instanceof ConsoleCommandSender) {
            consoleWarn(message, args);
        } else {
            sendMessage(to, message, args);
        }
    }

    public static void sendAutoDebug(CommandSender to, String message) {
        if (to instanceof Player) {
            sendMessageToPlayer((Player) to, message);
        } else if (to instanceof ConsoleCommandSender) {
            consoleDebug(message);
        } else {
            sendMessage(to, message);
        }
    }

    public static void sendAutoDebug(CommandSender to, String message, Object... args) {
        if (to instanceof Player) {
            sendMessageToPlayer((Player) to, message, args);
        } else if (to instanceof ConsoleCommandSender) {
            consoleDebug(message, args);
        } else {
            sendMessage(to, message, args);
        }
    }

    public static void sendAutoError(CommandSender to, String message) {
        if (to instanceof Player) {
            sendMessageToPlayer((Player) to, message);
        } else if (to instanceof ConsoleCommandSender) {
            consoleError(message);
        } else {
            sendMessage(to, message);
        }
    }

    public static void sendAutoError(CommandSender to, String message, Object... args) {
        if (to instanceof Player) {
            sendMessageToPlayer((Player) to, message, args);
        } else if (to instanceof ConsoleCommandSender) {
            consoleError(message, args);
        } else {
            sendMessage(to, message, args);
        }
    }

    public static void consoleInfo(String message, Object... args) {
        sendMessage(Bukkit.getConsoleSender(), consolePrefix + logInfo + message, args);
    }

    public static void consoleWarn(String message, Object... args) {
        sendMessage(Bukkit.getConsoleSender(), consolePrefix + logWarn + message, args);
    }

    public static void consoleDebug(String message, Object... args) {
        sendMessage(Bukkit.getConsoleSender(), consolePrefix + logDebug + message, args);
    }

    public static void consoleError(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        consoleError(sw.toString());
    }

    public static void consoleError(String message, Object... args) {
        sendMessage(Bukkit.getConsoleSender(), consolePrefix + logError + message, args);
    }

    public static void consoleError(String message, Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        consoleError(message);
        consoleError(sw.toString());
    }

    public static void consoleError(String message, Throwable e, Object... args) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        consoleError(message, args);
        consoleError(sw.toString());
    }

    public static void sendMessageToPlayer(Player to, String message) {
        sendMessage(to, inGamePrefix + formatMessageColor(message));
    }

    public static void sendMessageToPlayer(Player to, String message, Object... args) {
        sendMessage(to, inGamePrefix + formatMessageColor(formatMessageArgs(message, args)));
    }

    public static void sendNoPrefixMessageToPlayer(Player to, String message) {
        sendMessage(to, formatMessageColor(message));
    }

    public static void sendNoPrefixMessageToPlayer(Player to, String message, Object... args) {
        sendMessageToPlayer(to, formatMessageColor(formatMessageArgs(message, args)));
    }

    private static String formatMessageColor(String message){
        if (message == null || message.isEmpty()) {
            return message;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String formatMessageArgs(String message, Object... args) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        if (args.length == 0) {
            return message;
        }

        StringBuilder result = new StringBuilder();
        int argIndex = 0;
        int lastIndex = 0;
        int placeholderIndex;

        while (argIndex < args.length && (placeholderIndex = message.indexOf("{}", lastIndex)) != -1) {
            result.append(message, lastIndex, placeholderIndex);
            result.append(args[argIndex] == null ? "null" : args[argIndex].toString());
            lastIndex = placeholderIndex + 2;
            argIndex++;
        }

        if (lastIndex < message.length()) {
            result.append(message.substring(lastIndex));
        }

        return result.toString();
    }

}
