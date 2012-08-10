/*
 * Copyright (C) 2011-2012 lishid.  All rights reserved.
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

package lishid.openinv.commands;

import lishid.openinv.OpenInv;
import lishid.openinv.Permissions;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SearchInvPluginCommand implements CommandExecutor
{
    private final OpenInv plugin;
    
    public SearchInvPluginCommand(OpenInv plugin)
    {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (sender instanceof Player)
        {
            if (!sender.hasPermission(Permissions.PERM_SEARCH))
            {
                sender.sendMessage(ChatColor.RED + "You do not have permission to access player inventories");
                return true;
            }
        }
        
        String PlayerList = "";
        
        Material material = null;
        int count = 1;
        
        if (args.length >= 1)
        {
            String[] gData = null;
            gData = args[0].split(":");
            material = Material.matchMaterial(gData[0]);
        }
        if (args.length >= 2)
        {
            try
            {
                count = Integer.parseInt(args[1]);
            }
            catch (NumberFormatException ex)
            {
                sender.sendMessage(ChatColor.RED + "'" + args[1] + "' is not a number!");
                return false;
            }
        }
        
        if (material == null)
        {
            sender.sendMessage(ChatColor.RED + "Unknown item");
            return false;
        }
        
        for (Player templayer : plugin.getServer().getOnlinePlayers())
        {
            if (templayer.getInventory().contains(material, count))
            {
                PlayerList += templayer.getName() + "  ";
            }
        }
        
        sender.sendMessage("Players with the item " + material.toString() + ":  " + PlayerList);
        return true;
    }
}
