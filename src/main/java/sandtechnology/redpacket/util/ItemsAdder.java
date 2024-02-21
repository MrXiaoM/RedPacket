package sandtechnology.redpacket.util;

import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ItemsAdder {
    static Map<String, ItemStack> cache = new HashMap<>();
    static Boolean isPluginEnabled = null;
    private static boolean isEnabled() {
        if (isPluginEnabled != null) return isPluginEnabled;
        return (isPluginEnabled = Bukkit.getPluginManager().isPluginEnabled("ItemsAdder"));
    }

    public static ItemStack getItem(String id) {
        if (!isEnabled()) return new ItemStack(Material.PAPER);
        if (cache.containsKey(id)) return cache.get(id).clone();
        CustomStack stack = CustomStack.getInstance(id);
        if (stack == null) return new ItemStack(Material.PAPER);
        ItemStack item = stack.getItemStack();
        cache.put(id, item);
        return item;
    }
}
