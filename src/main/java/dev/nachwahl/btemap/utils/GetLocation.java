package dev.nachwahl.btemap.utils;

import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class GetLocation {

    private static EarthGeneratorSettings bteGeneratorSettings = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS);

    public static ArrayList<String> getAllLocations() throws OutOfProjectionBoundsException {
        ArrayList<String> players = new ArrayList<String>();
        for(Player p: Bukkit.getOnlinePlayers()){
            Location location = p.getLocation();
            UUID uuid = p.getUniqueId();


            double[] coordinates = bteGeneratorSettings.projection().toGeo(location.getX()*1,location.getZ()*1);

            String packet = uuid.toString()+";"+coordinates[0]+";"+coordinates[1];
            System.out.println(packet);
            players.add(packet);
        }
        return players;
    }


}
