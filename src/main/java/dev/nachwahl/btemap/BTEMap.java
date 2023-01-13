package dev.nachwahl.btemap;


import co.aikar.commands.PaperCommandManager;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.nachwahl.btemap.commands.MapCommand;
import dev.nachwahl.btemap.database.MySQLConnector;
import dev.nachwahl.btemap.listeners.LeaveEvent;
import dev.nachwahl.btemap.projection.GeographicProjection;
import dev.nachwahl.btemap.projection.ModifiedAirocean;
import dev.nachwahl.btemap.projection.ScaleProjection;
import dev.nachwahl.btemap.utils.FileBuilder;
import dev.nachwahl.btemap.utils.GetLocation;
import dev.nachwahl.btemap.utils.SocketIO;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

public final class BTEMap extends JavaPlugin {

    private MySQLConnector sqlConnector;
    private FileBuilder dbConfig;
    private SocketIO socketIO;

    @Override
    public void onEnable() {
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "bteplugin");
        dbConfig = new FileBuilder("plugins/PolyMap", "mysql.yml")
                .addDefault("mysql.host", "localhost")
                .addDefault("mysql.port", 3306)
                .addDefault("mysql.database", "map")
                .addDefault("mysql.username", "root")
                .addDefault("mysql.password", "")
                .addDefault("hostname", "localhost")
                .addDefault("port", 8899)
                .addDefault("token", "")
                .copyDefaults(true).save();
        sqlConnector = new MySQLConnector(dbConfig.getString("mysql.host"), dbConfig.getInt("mysql.port"),
                dbConfig.getString("mysql.database"), dbConfig.getString("mysql.username"), dbConfig.getString("mysql.password"));
        sqlConnector.connect();
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.enableUnstableAPI("help");
        manager.registerCommand(new MapCommand());

        Bukkit.getPluginManager().registerEvents(new LeaveEvent(this), this);

        this.socketIO = new SocketIO(dbConfig.getString("hostname"), dbConfig.getInt("port"), dbConfig.getString("token"));
        GetLocation loc = new GetLocation();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {


            ArrayList<String> players = new ArrayList<String>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                Location location = p.getLocation();
                UUID uuid = p.getUniqueId();
                double[] coordinates = toGeo(location.getX() * 1, location.getZ() * 1);
                String packet = uuid.toString() + ";" + coordinates[0] + ";" + coordinates[1] + ";" + p.getName();
                players.add(packet);
            }
            this.socketIO.sendPlayerLocationUpdate(String.valueOf(players));


        }, 0L, 20L);
    }

    public MySQLConnector getSqlConnector() {
        return sqlConnector;
    }

    public FileBuilder getDbConfig() {
        return dbConfig;
    }

    @Override
    public void onDisable() {
        if(this.socketIO != null) {
            this.socketIO.closeSocket();
        }

        this.getSqlConnector().disconnect();
    }

    public SocketIO getSocketIO() {
        return socketIO;
    }

    private static final GeographicProjection projection = new ModifiedAirocean();
    private static final GeographicProjection uprightProj = GeographicProjection.orientProjection(projection, GeographicProjection.Orientation.upright);
    private static final ScaleProjection scaleProj = new ScaleProjection(uprightProj, 7318261.522857145, 7318261.522857145);

    /**
     * Gets the geographical location from in-game coordinates
     *
     * @param x X-Axis in-game
     * @param z Z-Axis in-game
     * @return The geographical location (Long, Lat)
     */
    public static double[] toGeo(double x, double z) {
        return scaleProj.toGeo(x, z);
    }

    /**
     * Gets in-game coordinates from geographical location
     *
     * @param lon Geographical Longitude
     * @param lat Geographic Latitude
     * @return The in-game coordinates (x, z)
     */
    public static double[] fromGeo(double lon, double lat) {
        return scaleProj.fromGeo(lon, lat);
    }


    public void sendPluginMessage(String command, Player player, String... message) {
        //noinspection UnstableApiUsage
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF(command);
        for (String part : message) {
            output.writeUTF(part);
        }

        player.sendPluginMessage(this, "bteplugin", output.toByteArray());

    }
}


