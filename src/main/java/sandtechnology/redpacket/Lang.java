package sandtechnology.redpacket;

import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import sandtechnology.redpacket.util.ColorHelper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public enum Lang {
    ;
    static YamlConfiguration messages;
    final String key = name().toLowerCase().replace("__", ".").replace("_", "-");
    final String defaultValue;
    Lang(String... defaultValue) {
        this.defaultValue = String.join("\n", defaultValue);
    }

    public boolean t(CommandSender receiver, Object... replacements) {
        if (receiver == null) return false;
        receiver.sendMessage(text(replacements));
        return true;
    }

    /**
     * 获取语言值
     * @param replacements 变量替换器，偶数或零索引(0,2,4)为键，单数索引(1,3,5)为值。
     */
    public String text(Object... replacements) {
        String msg;
        if (messages.isList(key)) msg = String.join("\n&r", messages.getStringList(key));
        else msg = messages.getString(key, defaultValue);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            String key = replacements[i].toString();
            String value = replacements[i + 1].toString();
            msg = msg.replace(key, value);
        }
        return ColorHelper.parseColor(msg);
    }

    /**
     * 获取语言值
     * @param replacements 变量替换器，偶数或零索引(0,2,4)为键，单数索引(1,3,5)为值。
     */
    public List<String> list(Object... replacements) {
        String msg;
        if (messages.isList(key)) msg = String.join("\n&r", messages.getStringList(key));
        else msg = messages.getString(key, defaultValue);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            String key = replacements[i].toString();
            String value = replacements[i + 1].toString();
            msg = msg.replace(key, value);
        }
        return ColorHelper.parseColor(msg.split("\n"));
    }

    static void saveMessages(File file) throws IOException {
        if (messages == null) messages = new YamlConfiguration();
        for (Lang lang : values()) {
            if (messages.contains(lang.key)) continue;
            if (lang.defaultValue.contains("\n")) {
                List<String> value = Lists.newArrayList(lang.defaultValue.split("\n"));
                messages.set(lang.key, value);
            }
            messages.set(lang.key, lang.defaultValue);
        }
        messages.save(file);
    }
}
