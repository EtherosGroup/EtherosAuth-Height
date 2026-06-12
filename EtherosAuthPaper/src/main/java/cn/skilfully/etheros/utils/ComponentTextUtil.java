package cn.skilfully.etheros.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ComponentTextUtil {

    private static final LegacyComponentSerializer SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    public static Component text(Object text) {
        if (text == null) {
            return Component.empty();
        }
        if (text instanceof Component) {
            return (Component) text;
        }
        return SERIALIZER.deserialize(String.valueOf(text));
    }

}
