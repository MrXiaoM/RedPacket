package sandtechnology.redpacket;

import com.google.common.collect.Lists;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import sandtechnology.redpacket.util.ColorHelper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public enum Lang {
    COMMANDS__INVALID_ARGUMENT("&c命令参数不正确！"),
    COMMANDS__HELP(
            "&f----- &c&l红包插件&e 帮助&f ----",
            "&f/redpacket gui &e- 通过菜单界面发红包",
            "&f/redpacket [add/new] &e- 通过聊天界面发红包",
            "&f",
            "&7Powered by sandtechnology and MrXiaoM."
    ),
    COMMANDS__NEW__CREATING_SESSION("&a正在创建/拉取红包会话"),
    COMMANDS__QUERY__SELECT_TIPS_HOVER("点击以选择"),
    COMMANDS__QUERY__TYPE_TIPS("&a请选择红包类型"),
    COMMANDS__QUERY__TYPE__NORMAL("&a&n普通"),
    COMMANDS__QUERY__TYPE__PASSWORD("&a&n口令"),
    COMMANDS__QUERY__TYPE__JIE_LONG("&a&n接龙"),
    COMMANDS__QUERY__GIVE_TYPE_TIPS("&a请选择给予类型"),
    COMMANDS__QUERY__GIVE_TYPE__FIXED("&a&n固定"),
    COMMANDS__QUERY__GIVE_TYPE__LUCK("&a&n拼手气"),
    COMMANDS__QUERY__MONEY_TIPS("&a请在聊天栏发送红包总额（小数，比如 233.23）: "),
    COMMANDS__QUERY__AMOUNT_TIPS("&a请在聊天栏发送红包数量（整数，比如23）: "),
    COMMANDS__QUERY__GIVER_TIPS("&a请输入玩家名称（多个玩家请以英文逗号&f,&a分隔）: "),
    COMMANDS__QUERY__EXTRA_DATA_TIPS("&a请输入%extra_info%&a: "),
    COMMANDS__SESSION__CREATE__BROADCAST__NORMAL("&a玩家 &6%player_name%&a 发了一个 %type%! (%extra_key%: %extra_value%)"),
    COMMANDS__SESSION__CREATE__BROADCAST__LIMIT("&a玩家 &6%player_name%&a 发了一个专属 %limit_users% 的 %type%! (%extra_key%: %extra_value%)"),
    COMMANDS__SESSION__CANCEL("&e该会话已取消"),
    COMMANDS__RELOAD__SUCCESS("&a重载成功"),
    COMMANDS__RELOAD__FAILED("&c出现错误，请查看控制台"),
    REDPACKET__NORMAL__CLICK("&a&n点击这里领取"),
    REDPACKET__NORMAL__CLICK_HOVER("&e领取普通红包"),
    REDPACKET__PASSWORD__CLICK("&a&n点击这里领取"),
    REDPACKET__PASSWORD__CLICK_HOVER("&e发送口令:&f %password%"),
    REDPACKET__JIE_LONG__TIPS("&a下一个成语的音节为 &n%pin_yin%"),
    REDPACKET__NOTICE__NORMAL_TITLE("&a抢红包啦！"),
    REDPACKET__NOTICE__NORMAL_SUBTITLE("&a玩家 &6%player_name% &a发了一个%type%！"),
    REDPACKET__NOTICE__LIMIT_TITLE("&a抢红包啦！"),
    REDPACKET__NOTICE__LIMIT_SUBTITLE("&a玩家 &6%player_name% &a发了一个专属%type%！"),

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

    @SuppressWarnings({"deprecation"})
    public HoverEvent hover(Object... replacements) {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, bungees(replacements));
    }

    public BaseComponent[] bungees(Object... replacements) {
        return new BaseComponent[] { bungee(replacements) };
    }

    public BaseComponent bungee(Object... replacements) {
        return new TextComponent(TextComponent.fromLegacyText(text(replacements)));
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
