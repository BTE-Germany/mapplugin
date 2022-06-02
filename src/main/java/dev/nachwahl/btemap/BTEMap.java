package dev.nachwahl.btemap;


import co.aikar.commands.PaperCommandManager;
import dev.nachwahl.btemap.commands.MapCommand;
import dev.nachwahl.btemap.database.MySQLConnector;
import dev.nachwahl.btemap.utils.FileBuilder;
import dev.nachwahl.btemap.utils.GetLocation;
import dev.nachwahl.btemap.utils.SocketIO;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class BTEMap extends JavaPlugin {

    private MySQLConnector sqlConnector;
    private FileBuilder dbConfig;
    private SocketIO socketIO;

    @Override
    public void onEnable() {
        dbConfig = new FileBuilder("plugins/PolyMap", "mysql.yml")
                .addDefault("mysql.host", "localhost")
                .addDefault("mysql.port", 3306)
                .addDefault("mysql.database", "map")
                .addDefault("mysql.username", "root")
                .addDefault("mysql.password", "")
                .addDefault("hostname","localhost")
                .addDefault("port", 8899)
                .addDefault("token", "")
                .copyDefaults(true).save();
        sqlConnector = new MySQLConnector(dbConfig.getString("mysql.host"), dbConfig.getInt("mysql.port"),
                dbConfig.getString("mysql.database"), dbConfig.getString("mysql.username"), dbConfig.getString("mysql.password"));
        sqlConnector.connect();
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.enableUnstableAPI("help");
        manager.registerCommand(new MapCommand());

        this.socketIO = new SocketIO(dbConfig.getString("hostname"),dbConfig.getInt("port"),dbConfig.getString("token"));
        //GetLocation loc = new GetLocation(this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            getLogger().log(Level.INFO, "Sending data");

            try {
                this.socketIO.sendPlayerLocationUpdate(String.valueOf(GetLocation.getAllLocations()));
            } catch (OutOfProjectionBoundsException e) {
                e.printStackTrace();
            }

        }, 0L, 2L);
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
