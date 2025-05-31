package sandtechnology.redpacket.session;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import sandtechnology.redpacket.RedPacketPlugin;
import sandtechnology.redpacket.redpacket.RedPacket;
import sandtechnology.redpacket.util.EcoAndPermissionHelper;
import sandtechnology.redpacket.util.OperatorHelper;
import sandtechnology.redpacket.util.RedPacketManager;

import java.util.UUID;

import static sandtechnology.redpacket.util.CommonHelper.checkAndDoSomething;
import static sandtechnology.redpacket.util.CommonHelper.emptyFunction;
import static sandtechnology.redpacket.util.MessageHelper.sendSimpleMsg;

public class CreateSession {
    private final RedPacketPlugin plugin;
    private final RedPacket.Builder builder;
    private final UUID playerUUID;
    private final long expiredTime;
    private State state;

    CreateSession(RedPacketPlugin plugin, Player player) {
        this.plugin = plugin;
        this.playerUUID = player.getUniqueId();
        this.builder = new RedPacket.Builder(plugin, player);
        this.expiredTime = System.currentTimeMillis() + plugin.getConfig().getLong("RedPacket.SessionExpiredTime");
        this.state = State.Init;
    }

    public void cancel() {
        this.state = State.Cancel;
        plugin.getSessionManager().remove(this);
    }

    public State getState() {
        return this.state;
    }

    /**
     * 检查有效性并创建红包
     *
     * @return 创建的红包
     */
    public RedPacket create() {
        plugin.getSessionManager().remove(this);
        RedPacket redPacket = builder.build();
        EcoAndPermissionHelper.getEco().withdrawPlayer(redPacket.getPlayer(), redPacket.getMoney());
        plugin.getRedPacketManager().add(redPacket);
        return redPacket;
    }

    /**
     * parse玩家发送的信息
     *
     * @param player 玩家来源
     * @param data   信息
     */
    public void parse(Player player, String data) {
        try {
            switch (state) {
                case WaitExtra:
                    builder.extraData(data);
                    break;
                case WaitGiver:
                    builder.givers(data);
                    break;
                case WaitMoney:
                    builder.money(OperatorHelper.toTwoPrecision(Double.parseDouble(data)));
                    break;
                case WaitAmount:
                    builder.amount(Integer.parseInt(data));
                    break;
            }
        } catch (NumberFormatException e) {
            //去除颜色代码
            if (data.contains("§")) {
                data = data.replaceAll("§([0-9]|[A-z])", "").replaceAll("§", "");
                //重试
                parse(player, data);
            } else {
                sendSimpleMsg(player, ChatColor.RED, "数字格式错误！你输入的是" + data);
            }
        }
        setState(State.Init);
        sendSimpleMsg(player, builder.getInfo());
    }

    public boolean isUnexpired() {
        return checkAndDoSomething(expiredTime > System.currentTimeMillis(), emptyFunction, () -> plugin.getSessionManager().remove(this));
    }

    public boolean setState(State state) {
        return checkAndDoSomething(isUnexpired() && state != State.Cancel, () -> this.state = state,emptyFunction);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CreateSession && ((CreateSession) obj).builder.equals(this.builder);
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public RedPacket.Builder getBuilder() {
        return builder;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(expiredTime) + builder.hashCode();
    }

    public enum State {
        Init,
        WaitAmount,
        WaitMoney,
        WaitGiveType,
        WaitType,
        WaitExtra,
        WaitGiver,
        Cancel
    }
}