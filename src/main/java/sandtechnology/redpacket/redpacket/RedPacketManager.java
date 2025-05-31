package sandtechnology.redpacket.redpacket;

import sandtechnology.redpacket.RedPacketPlugin;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 红包管理
 */
public class RedPacketManager {
    private final RedPacketPlugin plugin;
    private final List<RedPacket> redPackets = new CopyOnWriteArrayList<>();
    public RedPacketManager(RedPacketPlugin plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        redPackets.addAll(plugin.getDatabaseManager().getValid());
        redPackets.forEach(RedPacket::refundIfExpired);
        plugin.getScheduler().runTaskTimerAsync(() -> redPackets.forEach(RedPacket::refundIfExpired),200,20000);
    }

    public void add(RedPacket redPacket) {
        plugin.getDatabaseManager().store(redPacket);
        redPackets.add(redPacket);
    }

    public void remove(RedPacket redPacket) {
        redPackets.remove(redPacket);
    }

    public List<RedPacket> getRedPackets() {
        return redPackets;
    }

}
