package dev.nachwahl.btemap.listeners;

import dev.nachwahl.btemap.BTEMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class LeaveEvent implements Listener {

    private final BTEMap plugin;

    public LeaveEvent(BTEMap plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLeave(@NotNull PlayerQuitEvent event) {
        this.plugin.getSocketIO().sendPlayerDisconnect(event.getPlayer().getUniqueId());
    }
}
