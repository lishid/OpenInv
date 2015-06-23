package com.lishid.openinv.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.Permissions;

public class SearchEnderCommand implements CommandExecutor {
    // TODO

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("searchinv")) {
            if (sender instanceof Player) {
                if (!OpenInv.hasPermission(sender, Permissions.PERM_SEARCH)) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to search player ender chests.");
                    return true;
                }
            }

            Material material = null;
            int count = 1;

            if (args.length >= 1) {
                String[] gData = null;
                gData = args[0].split(":");
                material = Material.matchMaterial(gData[0]);
            }
            if (args.length >= 2) {
                try {
                    count = Integer.parseInt(args[1]);
                }
                catch (NumberFormatException ex) {
                    sender.sendMessage(ChatColor.RED + "'" + args[1] + "' is not a number!");
                    return false;
                }
            }

            if (material == null) {
                sender.sendMessage(ChatColor.RED + "Unknown item");
                return false;
            }

            StringBuilder sb = new StringBuilder();

            for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                if (onlinePlayer.getInventory().contains(material, count)) {
                    sb.append(onlinePlayer.getName());
                    sb.append("  ");
                }
            }

            String playerList = sb.toString();
            sender.sendMessage("Players with the item " + material.toString() + ":  " + playerList);

            return true;
        }

        return false;
    }
}
