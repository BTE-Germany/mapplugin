package dev.nachwahl.btemap;


import co.aikar.commands.PaperCommandManager;
import dev.nachwahl.btemap.commands.MapCommand;
import dev.nachwahl.btemap.database.MySQLConnector;
import dev.nachwahl.btemap.utils.FileBuilder;
import org.bukkit.plugin.java.JavaPlugin;

public final class BTEMap extends JavaPlugin {

    private MySQLConnector sqlConnector;
    private FileBuilder dbConfig;

    @Override
    public void onEnable() {
        dbConfig = new FileBuilder("plugins/PolyMap", "mysql.yml")
                .addDefault("mysql.host", "localhost")
                .addDefault("mysql.port", 3306)
                .addDefault("mysql.database", "map")
                .addDefault("mysql.username", "root")
                .addDefault("mysql.password", "")
                .copyDefaults(true).save();
        sqlConnector = new MySQLConnector(dbConfig.getString("mysql.host"), dbConfig.getInt("mysql.port"),
                dbConfig.getString("mysql.database"), dbConfig.getString("mysql.username"), dbConfig.getString("mysql.password"));
        sqlConnector.connect();
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.enableUnstableAPI("help");
        manager.registerCommand(new MapCommand());

    }

    public MySQLConnector getSqlConnector() {
        return sqlConnector;
    }

    public FileBuilder getDbConfig() {
        return dbConfig;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
