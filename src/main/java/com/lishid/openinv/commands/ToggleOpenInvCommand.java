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

public class ToggleOpenInvCommand implements CommandExecutor {

    private final OpenInv plugin;
    private final Configuration configuration;

    public ToggleOpenInvCommand(OpenInv plugin) {
        this.plugin = plugin;
        configuration = plugin.getConfiguration();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("toggleopeninv")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You can't use this command from the console.");
                return true;
            }

            if (!OpenInv.hasPermission(sender, Permissions.PERM_OPENINV)) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to access player inventories.");
                return true;
            }

            Player player = (Player) sender;

            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("check")) {
                    String status = configuration.getPlayerItemOpenInvStatus(player) ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF";
                    plugin.sendMessage(player, "OpenInv with " + ChatColor.GRAY + configuration.getOpenInvItem() + ChatColor.RESET + status + ChatColor.RESET + ".");
                    return true;
                }
            }

            configuration.setPlayerItemOpenInvStatus(player, !configuration.getPlayerItemOpenInvStatus(player));

            String status = configuration.getPlayerItemOpenInvStatus(player) ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF";
            plugin.sendMessage(player, "OpenInv with " + ChatColor.GRAY + configuration.getOpenInvItem() + ChatColor.RESET + " is now " + status + ChatColor.RESET + ".");

            return true;
        }

        return false;
    }
}
