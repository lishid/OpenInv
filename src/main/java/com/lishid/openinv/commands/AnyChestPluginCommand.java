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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.Permissions;

public class AnyChestPluginCommand implements CommandExecutor {
    public AnyChestPluginCommand(OpenInv plugin) {

    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You can't use this from the console.");
            return true;
        }
        if (!OpenInv.hasPermission(sender, Permissions.PERM_ANYCHEST)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use anychest.");
            return true;
        }

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("check")) {
                if (OpenInv.GetPlayerAnyChestStatus(sender.getName()))
                    sender.sendMessage("AnyChest is ON.");
                else
                    sender.sendMessage("AnyChest is OFF.");
            }
        }

        OpenInv.SetPlayerAnyChestStatus(sender.getName(), !OpenInv.GetPlayerAnyChestStatus(sender.getName()));
        sender.sendMessage("AnyChest is now " + (OpenInv.GetPlayerAnyChestStatus(sender.getName()) ? "On" : "Off") + ".");

        return true;
    }
}
