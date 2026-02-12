package dev.nachwahl.btemap.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Subcommand;
import com.fastasyncworldedit.core.function.mask.AirMask;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import dev.nachwahl.btemap.BTEMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

@CommandAlias("map")
public class MapCommand extends BaseCommand {

    @Dependency
    private BTEMap plugin;
    private final Random random = new Random();

    @Subcommand("link")
    @CommandPermission("map.link")
    public void onLink(@NotNull CommandSender sender) {
        sender.sendMessage("§b§lBTEG §7» Please wait while we create your link code.");
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin,
                () -> {
                    int code = random.nextInt(900000) + 100000;
                    try {
                        boolean alreadyLinked;
                        try (PreparedStatement checkLinkedPs =
                                     plugin.getSqlConnector().getConnection().prepareStatement("SELECT 1 FROM User WHERE minecraftUUID = ?")) {
                            checkLinkedPs.setString(1, ((Player) sender).getUniqueId().toString());
                            alreadyLinked = checkLinkedPs.executeQuery().next();
                        }
                        if(alreadyLinked) {
                            sender.sendMessage("§b§lBTEG §7» §cThis account is already linked.");
                            return;
                        } else {

                            try (PreparedStatement deleteCodePs = plugin.getSqlConnector().getConnection().prepareStatement("DELETE FROM LinkCodes WHERE playerUUID = ?")) {
                                deleteCodePs.setString(1, ((Player) sender).getUniqueId().toString());
                                deleteCodePs.execute();
                            }

                            try (PreparedStatement ps = plugin.getSqlConnector().getConnection().prepareStatement("INSERT INTO `LinkCodes` (`id`, `code`, `playerUUID`, `createdAt`) VALUES (?, ?, ?, ?);")) {
                                ps.setString(1, UUID.randomUUID().toString());
                                ps.setString(2, String.valueOf(code));
                                ps.setString(3, ((Player) sender).getUniqueId().toString());
                                ps.setTimestamp(4, new Timestamp(new Date().getTime()));
                                ps.execute();
                            }
                        }

                    } catch (SQLException e) {
                        plugin.getComponentLogger().error("An error occurred while creating your code", e);
                        sender.sendMessage("§b§lBTEG §7» §cAn error occurred while creating your code");
                        return;
                    }
                    sender.sendMessage("§b§lBTEG §7» Your code is §b" + code + "§7. Please enter it on map.bte-germany.de");
                });
    }

    @Default
    @Subcommand("create")
    @CommandPermission("map.create")
    public void onCreate(@NotNull CommandSender sender){
        double lat = 0.0;
        double lon = 0.0;
        String city;
        Player player = (Player) sender;
        LocalSession sm = WorldEdit.getInstance().getSessionManager().findByName(player.getName());
        if(sm == null) {
            sender.sendMessage("§b§lBTEG §7» §cPlease select a region via WorldEdit first.");
            return;
        }
        Region region;
        try {
            region = sm.getSelection(WorldEdit.getInstance().getSessionManager().findByName(player.getName()).getSelectionWorld());
        } catch (IncompleteRegionException e) {
            sender.sendMessage("§b§lBTEG §7» §cPlease select a region via WorldEdit first.");
            return;
        }
        if(region == null) {
            sender.sendMessage("§b§lBTEG §7» §cPlease select a region via WorldEdit first.");
            return;
        }
        List<BlockVector2> poly;
        try {
            poly = region.polygonize(50);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§b§lBTEG §7» §cPlease select you region with under 50 points");
            return;
        }
        player.sendMessage("§b§lBTEG §7» One moment please...");

        StringBuilder coords = new StringBuilder("[");
        for (BlockVector2 vector2D : poly) {
            lat = BTEMap.toGeo(vector2D.x(), vector2D.z())[1];
            lon = BTEMap.toGeo(vector2D.x(), vector2D.z())[0];
            coords.append("[")
                    .append(BTEMap.toGeo(vector2D.x(), vector2D.z())[1])
                    .append(", ")
                    .append(BTEMap.toGeo(vector2D.x(), vector2D.z())[0])
                    .append("],");
        }
        coords.deleteCharAt(coords.length() - 1);
        coords.append("]");

        URI url;
        try {
            url = new URI("https://nominatim.openstreetmap.org/reverse?osm_type=N&format=json&zoom=18&lon=" + lon + "&accept-language=de&lat=" + lat);
            HttpURLConnection connection = (HttpURLConnection) url.toURL().openConnection();
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "BTE Germany Mapplugin");
            Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            for (int i; (i = reader.read()) >= 0;)
                response.append((char) i);

            JsonElement jsonElement = JsonParser.parseString(response.toString());
            if(jsonElement.getAsJsonObject().get("address").getAsJsonObject().get("city") != null) {
                city = jsonElement.getAsJsonObject().get("address").getAsJsonObject().get("city").getAsString();
            } else if (jsonElement.getAsJsonObject().get("address").getAsJsonObject().get("village") != null){
                city = jsonElement.getAsJsonObject().get("address").getAsJsonObject().get("village").getAsString();
            } else if (jsonElement.getAsJsonObject().get("address").getAsJsonObject().get("town") != null) {
                city = jsonElement.getAsJsonObject().get("address").getAsJsonObject().get("town").getAsString();
            } else {
                city = "n/A";
            }
        } catch (URISyntaxException | MalformedURLException e) {
            plugin.getComponentLogger().error("Error with OSM URL Syntax.", e);
            internalError(player, e.getMessage());
            return;
        } catch (IOException e) {
            plugin.getComponentLogger().error("I/O exception while map create.", e);
            internalError(player, e.getMessage());
            return;
        }

        try {
            region.contract(Vector3.at(0, region.getHeight() - 1d, 0).toBlockPoint());
        } catch (RegionOperationException e) {
            plugin.getComponentLogger().error("RegionOperationException exception while region contract.", e);
            internalError(player, e.getMessage());
        }

        Region finalRegion = region;
        String finalCoords = coords.toString();
        String finalCity = city;
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            UUID uuid = UUID.randomUUID();
            LocalSession localSession = WorldEdit.getInstance().getSessionManager().findByName(player.getName());

            // not sure if this is the right way to do it
            int counter = finalRegion.getWorld().countBlocks(finalRegion, new AirMask(new NullExtent()));
            player.sendMessage("§b§lBTEG §7» §aYour region has " + counter + " blocks");
            try {
                String id = null;
                try (PreparedStatement checkUserPs = plugin.getSqlConnector().getConnection().prepareStatement(
                        "SELECT id FROM User WHERE minecraftUUID = ?")) {
                    checkUserPs.setString(1, player.getUniqueId().toString());
                    ResultSet checkUserRs = checkUserPs.executeQuery();
                    if (checkUserRs.next()) {
                        id = checkUserRs.getString("id");
                    }
                }

                if(id != null) {
                    try (PreparedStatement ps = plugin.getSqlConnector().getConnection().prepareStatement("INSERT INTO Region (createdAt, id, username, userUUID, data, city, area, description, ownerID) VALUES (?, ?, ?, ?, ?, ?, ?, '', ?)")) {
                        preparePs(player, finalCoords, finalCity, uuid, counter, ps);
                        ps.setString(8, id);
                        ps.executeUpdate();
                    }
                } else {
                    PreparedStatement ps = plugin.getSqlConnector().getConnection().prepareStatement("INSERT INTO Region (createdAt, id, username, userUUID, data, city, area, description) VALUES (?, ?, ?, ?, ?, ?, ?, '')");
                    preparePs(player, finalCoords, finalCity, uuid, counter, ps);
                    ps.executeUpdate();
                }

                sm.getRegionSelector(localSession.getSelectionWorld()).clear();
                sender.sendMessage("§b§lBTEG §7» Your region was created successfully and is now visible on the map.");
                sender.sendMessage("§b§lBTEG §7» You can see your region by clicking on this link: https://map.bte-germany.de/?region=" + uuid + "&details=true");
            } catch (SQLException e) {
                plugin.getComponentLogger().error("An error occurred while publishing the region.", e);
                internalError(player, e.getMessage());
            }
        });
    }

    private void preparePs(@NotNull Player player, String finalCoords, String finalCity, @NotNull UUID uuid, int counter, @NotNull PreparedStatement ps) throws SQLException {
        ps.setTimestamp(1, new Timestamp(new Date().getTime()));
        ps.setString(2, uuid.toString());
        ps.setString(3, player.getName());
        ps.setString(4, player.getUniqueId().toString());
        ps.setString(5, finalCoords);
        ps.setString(6, finalCity);
        ps.setInt(7, counter);
    }

    private void internalError(@NotNull Player player, String message) {
        player.sendMessage(BTEMap.MSG_PREFIX.append(Component.text("Internal error accord, please report: " + message)));
    }
}
