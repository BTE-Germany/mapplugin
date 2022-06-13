package dev.nachwahl.btemap.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.nachwahl.btemap.BTEMap;
import dev.nachwahl.btemap.projection.GeographicProjection;
import dev.nachwahl.btemap.projection.ModifiedAirocean;
import dev.nachwahl.btemap.projection.ScaleProjection;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.UUID;

import static java.util.Collections.singletonMap;

class TeleportData {
    String uuid;
    double[] coords;
}
public class SocketIO {

    Socket socket;
    public SocketIO(String host, int port, String token){
        IO.Options options = IO.Options.builder()
                .setAuth(singletonMap("token", token))
                .build();


        try {
            this.socket = IO.socket(host+":"+port, options);
            this.socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        this.socket.on("teleportPlayer", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                TeleportData teleportData = new Gson().fromJson(args[0].toString(), TeleportData.class);
                String uuid = teleportData.uuid;
                double[] coords = teleportData.coords;
                if(coords.length == 2) {
                    try {
                        Player player = Bukkit.getServer().getPlayer(UUID.fromString(uuid));

                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

                        String coordinates = coords[0] + ", " + coords[1];
                        BTEMap.getPlugin(BTEMap.class).sendPluginMessage("tpll",player,player.getName(),coordinates);

                    } catch (Exception ignored) { }

                }
            }
        });
    }

    public void sendPlayerLocationUpdate(String data) {
        this.socket.emit("playerLocationUpdate", data);
    }

    public void closeSocket() {
        this.socket.close();
    }


    public void sendPlayerDisconnect(UUID uniqueId) {
        this.socket.emit("playerDisconnect", uniqueId.toString());
    }

}