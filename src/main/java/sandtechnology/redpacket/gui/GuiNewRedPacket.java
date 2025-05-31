package sandtechnology.redpacket.gui;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import sandtechnology.redpacket.Lang;
import sandtechnology.redpacket.RedPacketPlugin;
import sandtechnology.redpacket.session.CreateSession;

public class GuiNewRedPacket implements IGui {
    public final RedPacketPlugin plugin;
    public final FileConfiguration config;
    public final Player player;
    boolean confirm = false;
    CreateSession session;
    public GuiNewRedPacket(RedPacketPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.config = plugin.getConfig();
        session = plugin.getSessionManager().createSession(player);
    }
    @Override
    public Player getPlayer() {
        return player;
    }
    @Override
    public Inventory newInventory() {
        Inventory inv = Bukkit.createInventory(null, config.getInt("gui.size"), Lang.GUI__TITLE.textP(player));

        // TODO: 菜单图标

        return inv;
    }

    @Override
    public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot, ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onDrag(InventoryView view, InventoryDragEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onClose(InventoryView view) {
        if (!confirm) {
            session.cancel();
        }
    }
}
