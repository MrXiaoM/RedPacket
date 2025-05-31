package sandtechnology.redpacket;

import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import sandtechnology.redpacket.command.CommandHandler;
import sandtechnology.redpacket.database.AbstractDatabaseManager;
import sandtechnology.redpacket.database.MysqlManager;
import sandtechnology.redpacket.database.SqliteManager;
import sandtechnology.redpacket.listener.ChatListener;
import sandtechnology.redpacket.listener.GuiListener;
import sandtechnology.redpacket.listener.MessageSender;
import sandtechnology.redpacket.redpacket.RedPacketManager;
import sandtechnology.redpacket.session.SessionManager;
import sandtechnology.redpacket.util.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;

public class RedPacketPlugin extends JavaPlugin {
    private static RedPacketPlugin instance;
    private final FoliaLibScheduler scheduler;
    private AbstractDatabaseManager databaseManager;
    private GuiListener guiManager;
    private SessionManager sessionManager;
    private RedPacketManager redPacketManager;
    private boolean startup;

    public static RedPacketPlugin getInstance() {
        if (instance != null) {
            return instance;
        } else {
            throw new IllegalStateException("插件未正常开启！请查看报错信息");
        }
    }

    public GuiListener getGuiManager() {
        return guiManager;
    }

    public RedPacketPlugin() {
        instance = this;
        scheduler = new FoliaLibScheduler(this);
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public RedPacketManager getRedPacketManager() {
        return redPacketManager;
    }

    public AbstractDatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public void log(Level level, String msg, Object... format) {
        getLogger().log(level, String.format(msg, format));
    }

    public void warn(Throwable t) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
        }
        log(Level.WARNING, sw.toString());
    }

    public boolean reload() {
        try {
            reloadConfig();
            updateConfig();
            IdiomManager.reload(this);
            loadMessages();
            return true;
        } catch (Exception e) {
            warn(e);
            return false;
        }
    }

    public FoliaLibScheduler getScheduler() {
        return scheduler;
    }

    private void setIfAbsent(String node, Object value) {
        if (!getConfig().isSet(node)) {
            getConfig().set(node, value);
        }
    }

    private void updateConfig() {
        setIfAbsent("Version", 2);
        setIfAbsent("Database.Type", "sqlite");
        setIfAbsent("Database.FileName", "database.db");
        setIfAbsent("Database.IP", "127.0.0.1");
        setIfAbsent("Database.Port", 3306);
        setIfAbsent("Database.UserName", "");
        setIfAbsent("Database.Password", "");
        setIfAbsent("Database.DatabaseName", "database");
        setIfAbsent("Database.MySQLArgument", "");
        setIfAbsent("Database.TableName", "redpacket");
        setIfAbsent("RedPacket.MaxAmount", 10000);
        setIfAbsent("RedPacket.MaxMoney", 10000.0);
        setIfAbsent("RedPacket.MinMoney", 1.0);
        setIfAbsent("RedPacket.Expired", false);
        setIfAbsent("RedPacket.ExpiredTime", 86400000);
        setIfAbsent("RedPacket.SessionExpiredTime", 500000);
        setIfAbsent("gui.size", 54);
        setIfAbsent("gui.title", "发红包");
        saveConfig();
    }

    public void loadMessages() throws IOException {
        File file = new File(getDataFolder(), "messages.yml");
        if (!file.exists()) {
            Lang.saveMessages(file);
        }
        Lang.messages = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public void onEnable() {
        if (startup) {
            log(Level.WARNING, "检测到服务器重载，将使用重载逻辑！");
            reload();
        }
        try {
            saveDefaultConfig();
            FileConfiguration config = getConfig();

            getLogger().info("初始化插件...");
            CompatibilityHelper.setup(this);
            EcoAndPermissionHelper.setup(this);
            IdiomManager.setup(this);
            this.sessionManager = new SessionManager(this);
            this.redPacketManager = new RedPacketManager(this);

            getLogger().info("更新配置文件...");
            loadMessages();
            updateConfig();
            String tableName = config.getString("Database.TableName", "redpacket");
            if (config.getString("Database.Type", "sqlite").equalsIgnoreCase("sqlite")) {
                databaseManager = new SqliteManager(this, tableName);
            } else {
                databaseManager = new MysqlManager(this, tableName);
            }

            getLogger().info("注册监听器...");
            getServer().getPluginManager().registerEvents(new ChatListener(this), this);
            getServer().getPluginManager().registerEvents(new MessageSender(), this);
            guiManager = new GuiListener(this);

            getLogger().info("注册命令...");
            PluginCommand command = getCommand("RedPacket");
            if (command != null) {
                CommandHandler handler = new CommandHandler(this);
                command.setExecutor(handler);
                command.setTabCompleter(handler);
            } else {
                getLogger().warning("命令注册失败: 未找到");
            }
            getLogger().info("注册完成！等待其他插件加载完成...");
            // 为避免需要的经济插件被放在该插件后面加载造成出错
            // 将调用Vault API的方法延迟到服务器完全启动后
            getScheduler().runTask(() -> {
                getLogger().info("正在载入红包信息，请稍等...");
                redPacketManager.setup();
                MessageHelper.setStatus(this, true);
                getLogger().info("初始化插件完成！");
                startup = true;
            });
        } catch (Throwable e) {
            getServer().getPluginManager().disablePlugin(this);
            throw new RuntimeException("插件启用时发生了一个异常", e);
        }
    }

    @Override
    public void onDisable() {
        if (guiManager != null) guiManager.onDisable();
        if (startup) {
            getLogger().info("正在保存红包信息，请稍等...");
            databaseManager.setRunning(false);
            MessageHelper.setStatus(this, false);
            getLogger().info("完成！继续服务器关闭程序...");
        }
        scheduler.cancelTasks();
    }
}
