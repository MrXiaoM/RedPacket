package sandtechnology.redpacket.command;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import sandtechnology.redpacket.Lang;
import sandtechnology.redpacket.gui.GuiNewRedPacket;
import sandtechnology.redpacket.redpacket.RedPacket;
import sandtechnology.redpacket.session.CreateSession;
import sandtechnology.redpacket.util.IdiomManager;
import sandtechnology.redpacket.util.RedPacketManager;

import java.util.ArrayList;
import java.util.List;

import static sandtechnology.redpacket.RedPacketPlugin.getGui;
import static sandtechnology.redpacket.RedPacketPlugin.getInstance;
import static sandtechnology.redpacket.session.SessionManager.getSessionManager;
import static sandtechnology.redpacket.util.CommonHelper.checkAndDoSomething;
import static sandtechnology.redpacket.util.CommonHelper.emptyFunction;
import static sandtechnology.redpacket.util.EcoAndPermissionHelper.canSet;
import static sandtechnology.redpacket.util.EcoAndPermissionHelper.hasPermission;
import static sandtechnology.redpacket.util.MessageHelper.*;

public class CommandHandler implements TabExecutor {
    private static final CommandHandler commandHandler = new CommandHandler();

    public static CommandHandler getCommandHandler() {
        return commandHandler;
    }


    private boolean checkArgs(String[] args, int length, CommandSender sender) {
        return checkAndDoSomething(args.length >= length, emptyFunction, () -> Lang.COMMANDS__INVALID_ARGUMENT.t(sender));
    }

    private boolean checkSessionAndSetState(Player sender, CreateSession.State state) {
        return checkAndDoSomething(getSessionManager().hasSession(sender) && getSessionManager().getSession(sender).setState(state), emptyFunction, () -> sendSimpleMsg(sender, new ComponentBuilder(ChatColor.GREEN + "创建会话已失效，请点击这里重新创建！").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/redpacket new")).create()));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player && checkArgs(args, 1, sender)) {
            Player player = (Player) sender;
            switch (args[0].toLowerCase()) {
                case "add":
                case "new":
                    if (hasPermission(player, "redpacket.command.new")) {
                        Lang.COMMANDS__NEW__CREATING_SESSION.t(player);
                        sendSimpleMsg(player, getSessionManager().createSession(player).getBuilder().getInfo());
                    }
                    break;
                case "set":
                    if (checkArgs(args, 3, player) && getSessionManager().hasSession(player)) {
                        switch (args[1]) {
                            case "type":
                                switch (args[2].toLowerCase()) {
                                    case "normal":
                                        if (canSet(player, RedPacket.RedPacketType.CommonRedPacket)) {
                                            getSessionManager().getSession(player).getBuilder().type(RedPacket.RedPacketType.CommonRedPacket);
                                        }
                                        break;
                                    case "password":
                                        if (canSet(player, RedPacket.RedPacketType.PasswordRedPacket)) {
                                            getSessionManager().getSession(player).getBuilder().type(RedPacket.RedPacketType.PasswordRedPacket);
                                        }
                                        break;
                                    case "jielong":
                                        if (canSet(player, RedPacket.RedPacketType.JieLongRedPacket)) {
                                            getSessionManager().getSession(player).getBuilder().type(RedPacket.RedPacketType.JieLongRedPacket);
                                            getSessionManager().getSession(player).getBuilder().extraData(IdiomManager.getRandomIdiom());
                                        }
                                }
                                break;
                            case "givetype":
                                switch (args[2].toLowerCase()) {
                                    case "fixed":
                                        getSessionManager().getSession(player).getBuilder().giveType(RedPacket.GiveType.FixAmount);
                                        break;
                                    case "luck":
                                        getSessionManager().getSession(player).getBuilder().giveType(RedPacket.GiveType.LuckyAmount);
                                }
                        }
                        sendSimpleMsg(player, getSessionManager().getSession(player).getBuilder().getInfo());
                    }
                    break;
                case "password": {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        sb.append(" ").append(args[i]);
                    }
                    String msg = sb.toString().trim();
                    if (!msg.isEmpty()) {
                        player.chat(msg);
                    }
                    break;
                }
                case "gui":
                    getGui().openGui(new GuiNewRedPacket(player));
                    break;
                case "query":
                    if (checkArgs(args, 2, player)) {
                        HoverEvent selectTip = Lang.COMMANDS__QUERY__SELECT_TIPS_HOVER.hover();
                        switch (args[1].toLowerCase()) {
                            case "type":
                                if (checkSessionAndSetState(player, CreateSession.State.WaitType)) {
                                    Lang.COMMANDS__QUERY__TYPE_TIPS.t(player);
                                    sendSimpleMsg(player,
                                            new ComponentBuilder(Lang.COMMANDS__QUERY__TYPE__NORMAL.bungee()).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/redpacket set type normal")).event(selectTip)
                                                    .append("  ").reset()
                                                    .append(Lang.COMMANDS__QUERY__TYPE__PASSWORD.bungee()).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/redpacket set type password")).underlined(true).event(selectTip)
                                                    .append("  ").reset()
                                                    .append(Lang.COMMANDS__QUERY__TYPE__JIE_LONG.bungee()).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/redpacket set type jielong")).event(selectTip)
                                                    .create());
                                }
                                break;
                            case "givetype":
                                if (checkSessionAndSetState(player, CreateSession.State.WaitGiveType)) {
                                    Lang.COMMANDS__QUERY__GIVE_TYPE_TIPS.t(player);
                                    sendSimpleMsg(player,
                                            new ComponentBuilder(Lang.COMMANDS__QUERY__GIVE_TYPE__FIXED.bungee()).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/redpacket set givetype fixed")).event(selectTip)
                                                    .append("  ").reset()
                                                    .append(Lang.COMMANDS__QUERY__GIVE_TYPE__LUCK.bungee()).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/redpacket set givetype luck")).event(selectTip)
                                                    .create());
                                }
                                break;
                            case "money":
                                if (checkSessionAndSetState(player, CreateSession.State.WaitMoney)) {
                                    Lang.COMMANDS__QUERY__MONEY_TIPS.t(player);
                                }
                                break;
                            case "amount":
                                if (checkSessionAndSetState(player, CreateSession.State.WaitAmount)) {
                                    Lang.COMMANDS__QUERY__AMOUNT_TIPS.t(player);
                                }
                                break;
                            case "giver":
                                if (checkSessionAndSetState(player, CreateSession.State.WaitGiver)) {
                                    Lang.COMMANDS__QUERY__GIVER_TIPS.t(player);
                                }
                                break;
                            case "extradata":
                                if (checkSessionAndSetState(player, CreateSession.State.WaitExtra)) {
                                    Lang.COMMANDS__QUERY__EXTRA_DATA_TIPS.t(player,
                                            "%extra_info%",
                                            getSessionManager().getSession(player).getBuilder().getExtraDataInfo()
                                    );
                                }
                                break;
                            default:
                                Lang.COMMANDS__INVALID_ARGUMENT.t(player);
                        }
                    }
                    break;
                case "session":
                    if (checkArgs(args, 2, player) && checkSessionAndSetState(player, CreateSession.State.Init) && hasPermission(player, "redpacket.command.session")) {
                        switch (args[1].toLowerCase()) {
                            case "create":
                                Bukkit.getScheduler().runTaskAsynchronously(getInstance(), () -> {
                                    if (getSessionManager().getSession(player).getBuilder().isValid()) {
                                        RedPacket redPacket = getSessionManager().getSession(player).create();
                                        //生成提示信息
                                        BaseComponent text;
                                        if (redPacket.isLimitPlayer()) {
                                            text = Lang.COMMANDS__SESSION__CREATE__BROADCAST__LIMIT.bungee(
                                                    "%player_name%", player.getName(),
                                                    "%limit_users%", redPacket.getLimitPlayerList(),
                                                    "%type%", redPacket.getType().getName(),
                                                    "%extra_key%", redPacket.getType().getExtraDataName(),
                                                    "%extra_value%", redPacket.getExtraData()
                                            );
                                        } else {
                                            text = Lang.COMMANDS__SESSION__CREATE__BROADCAST__NORMAL.bungee(
                                                    "%player_name%", player.getName(),
                                                    "%type%", redPacket.getType().getName(),
                                                    "%extra_key%", redPacket.getType().getExtraDataName(),
                                                    "%extra_value%", redPacket.getExtraData()
                                            );
                                        }

                                        // 不含领取的提示信息
                                        final BaseComponent[] basicMessage = new ComponentBuilder(text).color(net.md_5.bungee.api.ChatColor.GREEN).create();
                                        // 含领取的提示信息
                                        final ComponentBuilder componentBuilder = new ComponentBuilder(text).color(net.md_5.bungee.api.ChatColor.GREEN);
                                        switch (redPacket.getType()) {
                                            case CommonRedPacket:
                                                componentBuilder.append(Lang.REDPACKET__NORMAL__CLICK.bungee())
                                                        .event(Lang.REDPACKET__NORMAL__CLICK_HOVER.hover())
                                                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/redpacket get " + redPacket.getUUID().toString()));
                                                break;
                                            case PasswordRedPacket:
                                                // 高版本 (1.19) 的 RUN_COMMAND 似乎无法发送聊天消息，故口令红包的点击快速领取改为命令
                                                componentBuilder.append(Lang.REDPACKET__PASSWORD__CLICK.bungees())
                                                        .event(Lang.REDPACKET__PASSWORD__CLICK_HOVER.hover("%password%", redPacket.getExtraData()))
                                                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/redpacket password " + redPacket.getExtraData()));
                                                break;
                                            case JieLongRedPacket:
                                                componentBuilder.append(Lang.REDPACKET__JIE_LONG__TIPS.bungee(
                                                        "%pin_yin%", IdiomManager.getIdiomPinyin(redPacket.getExtraData())
                                                ));
                                        }
                                        //对专享红包进行判断
                                        //防止游戏体验降低
                                        if (redPacket.isLimitPlayer()) {
                                            Bukkit.getScheduler().runTask(getInstance(), () -> {
                                                broadcastSelectiveRedPacket(redPacket.getLimitPlayers(),
                                                        Lang.REDPACKET__NOTICE__LIMIT_TITLE.text(),
                                                        Lang.REDPACKET__NOTICE__LIMIT_SUBTITLE.text(
                                                                "%player_name%", player.getName(),
                                                                "%type%", redPacket.getType().getName()
                                                        )
                                                );
                                            });
                                            redPacket.getLimitPlayers().forEach(offlinePlayer -> {
                                                sendServiceMsg(offlinePlayer, componentBuilder.create());
                                            });
                                            Bukkit.getOnlinePlayers().stream().filter(onlinePlayer -> !redPacket.getLimitPlayers().contains(onlinePlayer))
                                                    .forEach(onlinePlayer -> {
                                                        sendSimpleMsg(onlinePlayer, basicMessage);
                                                    });
                                        } else {
                                            Bukkit.getScheduler().runTask(getInstance(), () -> {
                                                broadcastRedPacket(
                                                        Lang.REDPACKET__NOTICE__NORMAL_TITLE.text(),
                                                        Lang.REDPACKET__NOTICE__NORMAL_SUBTITLE.text(
                                                                "%player_name%", player.getName(),
                                                                "%type%", redPacket.getType().getName()
                                                        )
                                                );
                                            });
                                            broadcastMsg(componentBuilder.create());
                                        }

                                    }
                                });
                                break;
                            case "cancel":
                                getSessionManager().getSession(player).cancel();
                                Lang.COMMANDS__SESSION__CANCEL.t(player);
                        }
                    }
                    break;
                case "get":
                    if (checkArgs(args, 2, player) && hasPermission(player, "redpacket.command.get")) {
                        Bukkit.getScheduler().runTaskAsynchronously(getInstance(), () -> RedPacketManager.getRedPacketManager().getRedPackets().stream().filter(packet -> packet.getUUID().toString().equals(args[1])).forEach(redPacket -> redPacket.giveIfValid(player, "")));
                    }
                case "info":
                    break;
                case "help":
                    Lang.COMMANDS__HELP.t(player);
                    break;
                case "reload":
                    if (hasPermission(player, "redpacket.command.reload")) {
                        checkAndDoSomething(getInstance().reload(), () -> Lang.COMMANDS__RELOAD__SUCCESS.t(player), () -> Lang.COMMANDS__RELOAD__FAILED.t(player));
                    }
                    break;

            }
        }
        return true;
    }

    private static final List<String> empty = new ArrayList<>();
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            if (sender.isOp()) {
                return startsWith(args[0], "new", "add", "gui", "reload");
            }
            return startsWith(args[0], "new", "add", "gui");
        }
        return empty;
    }

    public List<String> startsWith(String s, String... array) {
        List<String> list = new ArrayList<>();
        String s1 = s.toLowerCase();
        for (String s2 : array) {
            if (s2.toLowerCase().startsWith(s1)) list.add(s2);
        }
        return list;
    }
}
