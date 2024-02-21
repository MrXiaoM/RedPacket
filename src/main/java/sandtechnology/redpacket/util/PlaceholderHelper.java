package sandtechnology.redpacket.util;

import com.google.common.collect.Lists;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class PlaceholderHelper {
    static Boolean isPluginEnabled = null;
    private static boolean isEnabled() {
        if (isPluginEnabled != null) return isPluginEnabled;
        return (isPluginEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"));
    }

    public static String setPlaceholders(Player player, String s) {
        if (!isEnabled()) return s.replace("%player_name%", player.getName());
        return PlaceholderAPI.setPlaceholders(player, s);
    }

    public static List<String> setPlaceholders(Player player, List<String> list) {
        if (!isEnabled()) return Lists.newArrayList(String.join("\n").replace("%player_name%", player.getName()).split("\n"));
        return PlaceholderAPI.setPlaceholders(player, list);
    }
}
