/*
 * Copyright (C) 2011-2014 lishid.  All rights reserved.
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
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.Permissions;

public class ToggleOpenInvPluginCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You can't use this from the console.");
            return true;
        }
        if (!OpenInv.hasPermission(sender, Permissions.PERM_OPENINV)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to access player inventories");
            return true;
        }

        Player player = (Player) sender;
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("check")) {
                if (OpenInv.GetPlayerItemOpenInvStatus(player.getName()))
                    player.sendMessage("OpenInv with " + Material.getMaterial(OpenInv.GetItemOpenInvItem()).toString() + " is ON.");
                else
                    player.sendMessage("OpenInv with " + Material.getMaterial(OpenInv.GetItemOpenInvItem()).toString() + " is OFF.");
            }
        }
        if (OpenInv.GetPlayerItemOpenInvStatus(player.getName())) {
            OpenInv.SetPlayerItemOpenInvStatus(player.getName(), false);
            player.sendMessage("OpenInv with " + Material.getMaterial(OpenInv.GetItemOpenInvItem()).toString() + " is OFF.");
        }
        else {
            OpenInv.SetPlayerItemOpenInvStatus(player.getName(), true);
            player.sendMessage("OpenInv with " + Material.getMaterial(OpenInv.GetItemOpenInvItem()).toString() + " is ON.");
        }
        return true;
    }
}
