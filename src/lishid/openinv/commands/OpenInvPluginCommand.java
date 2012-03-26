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

import java.io.File;
import java.util.HashMap;

import lishid.openinv.OpenInv;
import lishid.openinv.utils.PlayerInventoryChest;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class OpenInvPluginCommand implements CommandExecutor {
    private final OpenInv plugin;
    public static HashMap<Player, PlayerInventoryChest> offlineInv = new HashMap<Player, PlayerInventoryChest>();
    public static HashMap<Player, String> openInvHistory = new HashMap<Player, String>();
    public OpenInvPluginCommand(OpenInv plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if(!(sender instanceof Player))
    	{
            sender.sendMessage(ChatColor.RED + "You can't use this from the console.");
    		return true;
    	}
    	if (!sender.hasPermission("OpenInv.openinv")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to access player inventories");
            return true;
        }
    	
    	if(args.length > 0 && args[0].equalsIgnoreCase("?"))
    	{
    		OpenInv.ShowHelp((Player)sender);
    		return true;
    	}
        
		Player player = (Player)sender;
    	boolean Offline = false;
    	
		//History management
		String history = openInvHistory.get(player);
		
		if(history == null || history == "")
		{
			history = player.getName();
			openInvHistory.put(player, history);
		}

		//Target selecting
		Player target;
		
		String name = "";
		
		//Read from history if target is not named
		if (args.length < 1) {
			if(history != null && history != "")
			{
				name = history;
			}
			else
			{
				sender.sendMessage(ChatColor.RED + "OpenInv history is empty!");
				return true;
			}
		}
		else
		{
			name = args[0];
		}
		
		target = this.plugin.getServer().getPlayer(name);

		if(target == null)
		{
			//Offline inv here...
			try{
				//See if the player has data files
				
				// Find the player folder
				File playerfolder = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "players");

				// Find player name
				for (File playerfile : playerfolder.listFiles())
				{
					String filename = playerfile.getName();
					String playername = filename.substring(0, filename.length() - 4);
					
					if(playername.trim().equalsIgnoreCase(name))
					{
						//Create an entity to load the player data
						MinecraftServer server = ((CraftServer)this.plugin.getServer()).getServer();
						EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), playername, new ItemInWorldManager(server.getWorldServer(0)));
						target = (entity == null) ? null : (Player) entity.getBukkitEntity();
						if(target != null)
						{
							Offline = true;
							target.loadData();
						}
						else
						{
							sender.sendMessage(ChatColor.RED + "Player not found!");
							return true;
						}
					}
				}
				if(!Offline)
				{
					sender.sendMessage(ChatColor.RED + "Player not found!");
					return true;
				}
			}
			catch(Exception e)
			{
				sender.sendMessage("Error while retrieving offline player data!");
				e.printStackTrace();
				return true;
			}
		}
		
		//Permissions checks
		if (!player.hasPermission("OpenInv.override") && target.hasPermission("OpenInv.exempt")) {
            sender.sendMessage(ChatColor.RED + target.getDisplayName() + "'s inventory is protected!");
            return true;
        }
		
		if((!player.hasPermission("OpenInv.crossworld") && !player.hasPermission("OpenInv.override")) && 
				target.getWorld() != player.getWorld()){
			sender.sendMessage(ChatColor.RED + target.getDisplayName() + " is not in your world!");
            return true;
		}

		//Record the target
		history = target.getName();
		openInvHistory.put(player, history);
		
		//Get the EntityPlayer handle from the sender
		EntityPlayer entityplayer = ((CraftPlayer) player).getHandle();

		//Get the EntityPlayer from the Target
		EntityPlayer entitytarget = ((CraftPlayer) target).getHandle();
		
		//Create the inventory
		PlayerInventoryChest inv = new PlayerInventoryChest(entitytarget.inventory, entitytarget);
		
		//Save data into the inventory for tracking
		inv.Opener = player;
		inv.Target = target;
		
		//Saves offline openinv
		if(Offline)
		{
			inv.Offline = true;
			offlineInv.put(target, inv);
		}
		
		//Open the inventory
		entityplayer.openContainer(inv);
		
		return true;
    }
}
