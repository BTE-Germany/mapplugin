package dev.nachwahl.btemap.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import dev.nachwahl.btemap.BTEMap;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

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

            }
        });
    }

    @HelpCommand
    public static void onHelp(CommandSender sender, CommandHelp help) {
        sender.sendMessage("§6BTE Germany Map");
        help.showHelp();
    }




}
