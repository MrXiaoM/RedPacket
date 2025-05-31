package sandtechnology.redpacket.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import sandtechnology.redpacket.RedPacketPlugin;
import sandtechnology.redpacket.redpacket.RedPacket;
import sandtechnology.redpacket.session.CreateSession;
import sandtechnology.redpacket.session.SessionManager;

import java.util.Arrays;

public class ChatListener implements Listener {
    private static final CreateSession.State[] inputNeededState = {
            CreateSession.State.WaitAmount,
            CreateSession.State.WaitExtra,
            CreateSession.State.WaitGiver,
            CreateSession.State.WaitMoney
    };
    private final RedPacketPlugin plugin;
    public ChatListener(RedPacketPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        SessionManager sessionManager = plugin.getSessionManager();
        //判断是否在输入创建红包的数据
        if (sessionManager.hasSession(player) && Arrays.stream(inputNeededState).anyMatch(state -> state == sessionManager.getSession(player).getState())) {
            sessionManager.getSession(player).parse(event.getPlayer(),event.getMessage());
            event.setCancelled(true);
        }
        //确保异步执行
        if (event.isAsynchronous()){
           checkRedPacket(event);
        } else {
            plugin.getScheduler().runTaskAsync(() -> checkRedPacket(event));
        }
    }

    private void checkRedPacket(AsyncPlayerChatEvent event){
        plugin.getRedPacketManager().getRedPackets().stream().filter(redPacket -> redPacket.getType().equals(RedPacket.RedPacketType.JieLongRedPacket) || redPacket.getType().equals(RedPacket.RedPacketType.PasswordRedPacket)).forEach(redPacket -> redPacket.giveIfValid(event.getPlayer(), event.getMessage()));
    }

}
