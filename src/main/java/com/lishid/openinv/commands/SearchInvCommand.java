/*
 * Copyright (C) 2011-2016 lishid.  All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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

public class SearchInvCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("searchinv")) {
            if (sender instanceof Player) {
                if (!OpenInv.hasPermission(sender, Permissions.PERM_SEARCH)) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to search player inventories.");
                    return true;
                }
            }

            Material material = null;
            int count = 1;

            if (args.length >= 1) {
                String[] gData;
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
            sender.sendMessage("Players with the item " + ChatColor.GRAY + material.toString() + ChatColor.RESET + " in their inventory:  " + playerList);

            return true;
        }

        return false;
    }
}
