package dev.nachwahl.btemap.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import dev.nachwahl.btemap.BTEMap;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

@CommandAlias("map")
public class MapCommand extends BaseCommand {

    @Dependency
    private BTEMap plugin;

    @Subcommand("link")
    @CommandPermission("map.link")
    public void onLink(CommandSender sender) {
        sender.sendMessage("§b§lBTEG §7» Please wait while we create your link code.");
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
            @Override
            public void run() {
                int code = new Random().nextInt(900000) + 100000;
                try {
                    PreparedStatement checkLinkedPs = plugin.getSqlConnector().getConnection().prepareStatement("SELECT * FROM user WHERE minecraftUUID = ?");
                    checkLinkedPs.setString(1, ((Player) sender).getUniqueId().toString());
                    ResultSet checkLinkedRs = checkLinkedPs.executeQuery();
                    if(checkLinkedRs.next()) {
                        sender.sendMessage("§b§lBTEG §7» §cThis account is already linked.");
                        return;
                    } else {
                        PreparedStatement ps = plugin.getSqlConnector().getConnection().prepareStatement("INSERT INTO `linkcodes` (`id`, `code`, `playerUUID`, `createdAt`) VALUES (?, ?, ?, ?);");
                        ps.setString(1, UUID.randomUUID().toString());
                        ps.setString(2, String.valueOf(code));
                        ps.setString(3, ((Player) sender).getUniqueId().toString());
                        ps.setTimestamp(4, new Timestamp(new Date().getTime()));
                        ps.execute();
                    }

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                    sender.sendMessage("§b§lBTEG §7» §cAn error occurred while creating your code");
                    return;
                }
                sender.sendMessage("§b§lBTEG §7» Your code is §b" + code + "§7. Please enter it on map.bte-germany.de");

            }
        });
    }

    @Default
    public static void onDefault(CommandSender sender){
        BTEMap.getPlugin(BTEMap.class).sendPluginMessage("nwarp", (Player) sender,sender.getName(),"Map");
    }

    @HelpCommand
    public static void onHelp(CommandSender sender, CommandHelp help) {
        sender.sendMessage("§6BTE Germany Map");
        help.showHelp();
    }




}
