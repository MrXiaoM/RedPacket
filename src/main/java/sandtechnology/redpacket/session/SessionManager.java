package sandtechnology.redpacket.session;

import org.bukkit.entity.Player;
import sandtechnology.redpacket.RedPacketPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话管理
 */
public class SessionManager {
    private final RedPacketPlugin plugin;
    private final Map<UUID,CreateSession> sessions = new ConcurrentHashMap<>();
    public SessionManager(RedPacketPlugin plugin) {
        this.plugin = plugin;
    }

    public Map<UUID, CreateSession> getSessions() {
        return sessions;
    }

    private CreateSession add(CreateSession createSession){
        sessions.put(createSession.getPlayerUUID(),createSession);
        return createSession;
    }

    public void remove(CreateSession createSession){
        sessions.remove(createSession.getPlayerUUID());
    }

    public void remove(Player player){
        sessions.remove(player.getUniqueId());
    }

    public boolean hasSession(Player player){
        return sessions.containsKey(player.getUniqueId());
    }

    public CreateSession createSession(Player player){
        if (hasSession(player) && getSession(player).isUnexpired()) {
            return getSession(player);
        }else {
            CreateSession session=new CreateSession(plugin, player);
            sessions.put(player.getUniqueId(),session);
            return session;
        }
    }

    public CreateSession getSession(Player player){
        return sessions.get(player.getUniqueId());
    }
}
