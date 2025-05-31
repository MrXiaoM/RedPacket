package sandtechnology.redpacket.database;

import sandtechnology.redpacket.RedPacketPlugin;

import java.sql.DriverManager;

public class SqliteManager extends AbstractDatabaseManager {
    private final RedPacketPlugin plugin;
    public SqliteManager(RedPacketPlugin plugin, String tableName) {
        this.plugin = plugin;
        setup(tableName);
    }

    @Override
    public RedPacketPlugin getPlugin() {
        return plugin;
    }

    @Override
    void setup(String tableName) {
        try{
            Class.forName("org.sqlite.JDBC");
            this.tableName = tableName;
            String databaseFileName = plugin.getConfig().getString("Database.FileName", "database.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().toPath().resolve(databaseFileName));
            executeUpdate(
                    "create table if not exists " + tableName + " (" +
                            "UUID Text PRIMARY KEY," +
                            "playerUUID Text NOT NULL," +
                            "giveType Text NOT NULL," +
                            "RedPacketType Text NOT NULL," +
                            "amount INTEGER NOT NULL," +
                            "money real NOT NULL," +
                            "moneyMap Text NOT NULL," +
                            "extraData Text NOT NULL," +
                            "givers Text NOT NULL," +
                            "expireTime INTEGER NOT NULL," +
                            "timeZone TEXT NOT NULL,"+
                            "expired INTEGER NOT NULL)"
            );
            executeUpdate("CREATE INDEX if not exists searchIndex ON " + tableName + " (playerUUID, expireTime)");
            connection.setAutoCommit(false);
            setRunning(true);
            startCommitTimer();
        } catch (Exception ex) {
            throw new RuntimeException("数据库初始化出现错误，将关闭本插件！", ex);
        }
    }
}
