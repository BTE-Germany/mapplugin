package dev.nachwahl.btemap.utils;

import com.google.gson.Gson;
import dev.nachwahl.btemap.BTEMap;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
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

        this.socket.on("teleportPlayer",
                args -> {
                    TeleportData teleportData = new Gson().fromJson(args[0].toString(), TeleportData.class);
                    String uuid = teleportData.uuid;
                    double[] coords = teleportData.coords;
                    if(coords.length == 2) {
                        try {
                            Player player = Bukkit.getServer().getPlayer(UUID.fromString(uuid));

                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

                            String coordinates = coords[0] + ", " + coords[1];
                            JavaPlugin.getPlugin(BTEMap.class).sendPluginMessage("tpll",player,player.getName(),coordinates);

                        } catch (Exception ignored) { /* ignored */ }

                    }
                });
    }

    public void sendPlayerLocationUpdate(String data) {
        this.socket.emit("playerLocationUpdate", data);
    }

    public void closeSocket() {
        this.socket.close();
    }

    public void sendPlayerDisconnect(@NotNull UUID uniqueId) {
        this.socket.emit("playerDisconnect", uniqueId.toString());
    }
}