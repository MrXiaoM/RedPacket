package sandtechnology.redpacket.util;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import sandtechnology.redpacket.RedPacketPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;
import static sandtechnology.redpacket.RedPacketPlugin.getInstance;

@SuppressWarnings({"rawtypes", "SameParameterValue"})
public class CompatibilityHelper {
    // NMS名： "org.bukkit.craftbukkit.v1_x_Rx"->{"org","bukkit","craftbukkit","v1_x_Rx"}->"v1_x_Rx"
    private static String nmsName;
    /*
    基于NMS名的版本提取
    v1_8_R1->8
    */
    private static int version;

    private static Class<?> entityPlayer;
    private static Enum<? extends Enum>[] EnumTitleActions;
    private static Method getHandle;
    private static Method sendMessage;
    private static Method toComponent;
    private static Method sendPacket;
    private static Constructor<?> CPacketPlayOutTitle;

    private CompatibilityHelper() {
    }


    private static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + nmsName + "." + name);
    }

    public static void setup(Logger logger) {
        try {
            nmsName = getServer().getClass().getPackage().getName().split("\\.")[3];
            version = Integer.parseInt(nmsName.split("_")[1]);
        } catch (Throwable t) {
            // 因为 Paper 在高版本取消了 relocation
            // 如果获取 NMS 版本报错，默认当前版本为 1.20+
            version = 20;
        }

        if (version <= 7) {
            RedPacketPlugin.log(Level.SEVERE, "插件只支持1.8+版本！");
            throw new IllegalStateException("插件只支持1.8+版本！");
        }
        if (version >= 12) {
            // 1.12 及以上不需要NMS反射
            return;
        }
        try {
            entityPlayer = getNMSClass("EntityPlayer");
            Class<?> chatSerializer = getNMSClass("IChatBaseComponent$ChatSerializer");
            Class<?> IChatBaseComponent = getNMSClass("IChatBaseComponent");
            Class<?> packetPlayOutTitle = getNMSClass("PacketPlayOutTitle");
            Class<?> playerConnection = getNMSClass("PlayerConnection");
            Class<?> craftPlayer = Class.forName("org.bukkit.craftbukkit." + nmsName + "." + "entity.CraftPlayer");
            getHandle = craftPlayer.getMethod("getHandle");
            sendMessage = entityPlayer.getMethod("sendMessage", IChatBaseComponent);
            toComponent = chatSerializer.getMethod("a", String.class);
            sendPacket = playerConnection.getMethod("sendPacket", getNMSClass("Packet"));
            Class<?> enumTitleAction = Arrays.stream(packetPlayOutTitle.getClasses()).filter(Class::isEnum).collect(Collectors.toList()).get(0);
            EnumTitleActions = (Enum<? extends Enum>[]) enumTitleAction.getEnumConstants();
            CPacketPlayOutTitle = packetPlayOutTitle.getConstructor(enumTitleAction, IChatBaseComponent);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "初始化 NMS 实现时出现异常 (" + nmsName + ": v" + version + ")", e);
        }
    }

    private static Object invoke(Method method, Object obj, Object... args) {
        try {
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw new RuntimeException("在反射调用方法时发生错误！" + method.getName(), e);
        }
    }

    private static Object newInstance(Constructor<?> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("在反射实例化类时发生错误！类名：", e);
        }
    }

    public static void playLevelUpSound(Player player) {
        if (version > 8) {
            playSound(player, "ENTITY_PLAYER_LEVELUP");
        } else {
            playSound(player, "LEVEL_UP");
        }
    }

    public static void playMeowSound(Player player) {
        if (version > 8) {
            playSound(player, "ENTITY_CAT_AMBIENT");
        } else {
            playSound(player, "CAT_MEOW");
        }
    }

    private static void playSound(Player player, String name) {
        player.playSound(player.getLocation(), Sound.valueOf(name), 1.0f, 1.0f);
    }

    private static Object getDeclaredFieldAndGetIt(Class<?> target, String field, Object instance) {
        try {
            return target.getDeclaredField(field).get(instance);
        } catch (Exception e) {
            throw new RuntimeException("在反射获取字段时发生错误！方法名：", e);
        }
    }

    public static void sendTitle(Player player, String title, String subtitle) {
        if (version >= 11) {
            player.sendTitle(title, subtitle, -1, -1, -1);
        } else {
            if (version >= 8) {
                //反射需要较长时间，采取异步处理再发送消息
                getInstance().getScheduler().runTaskAsync(() -> {
                    Object connectionInstance = getDeclaredFieldAndGetIt(entityPlayer, "playerConnection", invoke(getHandle, player));
                    Object titlePacket = newInstance(CPacketPlayOutTitle, EnumTitleActions[0], invoke(toComponent, null, ComponentSerializer.toString(new TextComponent(title))));
                    Object subtitlePacket = newInstance(CPacketPlayOutTitle, EnumTitleActions[1], invoke(toComponent, null, ComponentSerializer.toString(new TextComponent(subtitle))));
                    getInstance().getScheduler().runTask(() -> {
                        invoke(sendPacket, connectionInstance, titlePacket);
                        invoke(sendPacket, connectionInstance, subtitlePacket);
                    });
                });
            }
        }
    }

    public static void sendJSONMessage(Player player, BaseComponent... components) {
        if (version >= 12) {
            player.spigot().sendMessage(components);
        } else {
            if (version >= 7) {
                //https://www.spigotmc.org/threads/get-player-ping-with-reflection.147773/
                //反射需要较长时间，采取异步处理再发送消息
                getInstance().getScheduler().runTaskAsync(() -> {
                    Object playerInstance = invoke(getHandle, player);
                    Object JSONString = invoke(toComponent, null, ComponentSerializer.toString(components));
                    getInstance().getScheduler().runTask(() -> invoke(sendMessage, playerInstance, JSONString));
                });
            }
        }
    }

}
