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

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.Permissions;
import com.lishid.openinv.Configuration;

public class AnyChestCommand implements CommandExecutor {

    private final OpenInv plugin;
    private final Configuration configuration;

    public AnyChestCommand(OpenInv plugin) {
        this.plugin = plugin;
        configuration = plugin.getConfiguration();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("anychest")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You can't use this command from the console.");
                return true;
            }

            if (!OpenInv.hasPermission(sender, Permissions.PERM_ANYCHEST)) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use any chest.");
                return true;
            }

            Player player = (Player) sender;

            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("check")) {
                    String status = configuration.getPlayerAnyChestStatus(player) ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF";
                    OpenInv.sendMessage(player, "Any Chest is " + status + ChatColor.RESET + ".");
                    return true;
                }
            }

            configuration.setPlayerAnyChestStatus(player, !configuration.getPlayerAnyChestStatus(player));

            String status = configuration.getPlayerAnyChestStatus(player) ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF";
            OpenInv.sendMessage(player, "Any Chest is now " + status + ChatColor.RESET + ".");

            return true;
        }

        return false;
    }
}
