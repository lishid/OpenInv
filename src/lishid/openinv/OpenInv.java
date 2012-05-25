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

package lishid.openinv;

import lishid.openinv.commands.*;
import lishid.openinv.utils.Metrics;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import balor.OpenInv.InventoryManager;

/**
 * Open other player's inventory
 *
 * @author lishid
 */
public class OpenInv extends JavaPlugin {
	private final OpenInvPlayerListener playerListener = new OpenInvPlayerListener(this);
	private final OpenInvEntityListener entityListener = new OpenInvEntityListener(this);
	public static OpenInv mainPlugin;
	private static Metrics metrics;
	
    public void onDisable() {
    }

    public void onEnable() {
    	mainPlugin = this;
    	mainPlugin.getConfig().addDefault("ItemOpenInvItemID", 280);
    	mainPlugin.getConfig().options().copyDefaults(true);
    	mainPlugin.saveConfig();

    	InventoryManager.createInstance();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(playerListener, this);
		pm.registerEvents(entityListener, this);


        getCommand("openinv").setExecutor(new OpenInvPluginCommand(this));
        getCommand("searchinv").setExecutor(new SearchInvPluginCommand(this));
        getCommand("toggleopeninv").setExecutor(new ToggleOpenInvPluginCommand());
        getCommand("silentchest").setExecutor(new SilentChestPluginCommand(this));
        getCommand("anychest").setExecutor(new AnyChestPluginCommand(this));
        
		//Metrics
		try
		{
			metrics = new Metrics(this);
			metrics.start();
		}
		catch(Exception e){ e.printStackTrace(); }

        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println("[" + pdfFile.getName() + "] version " + pdfFile.getVersion() + " enabled!" );
    }
    
    public static boolean GetPlayerItemOpenInvStatus(String name)
    {
    	return mainPlugin.getConfig().getBoolean("ItemOpenInv." + name.toLowerCase() + ".toggle", false);
    }
    
    public static void SetPlayerItemOpenInvStatus(String name, boolean status)
    {
    	mainPlugin.getConfig().set("ItemOpenInv." + name.toLowerCase() + ".toggle", status);
    	mainPlugin.saveConfig();
    }

    public static boolean GetPlayerSilentChestStatus(String name)
    {
    	return mainPlugin.getConfig().getBoolean("SilentChest." + name.toLowerCase() + ".toggle", false);
    }
    
    public static void SetPlayerSilentChestStatus(String name, boolean status)
    {
    	mainPlugin.getConfig().set("SilentChest." + name.toLowerCase() + ".toggle", status);
    	mainPlugin.saveConfig();
    }

    public static boolean GetPlayerAnyChestStatus(String name)
    {
    	return mainPlugin.getConfig().getBoolean("AnyChest." + name.toLowerCase() + ".toggle", true);
    }
    
    public static void SetPlayerAnyChestStatus(String name, boolean status)
    {
    	mainPlugin.getConfig().set("AnyChest." + name.toLowerCase() + ".toggle", status);
    	mainPlugin.saveConfig();
    }
    
    public static int GetItemOpenInvItem()
    {
		if(mainPlugin.getConfig().get("ItemOpenInvItemID") == null)
		{
			SaveToConfig("ItemOpenInvItemID", 280);
		}
    	return mainPlugin.getConfig().getInt("ItemOpenInvItemID", 280);
    }
    
    public static Object GetFromConfig(String data, Object defaultValue)
    {
    	Object val = mainPlugin.getConfig().get(data);
        if (val == null)
        {
        	mainPlugin.getConfig().set(data, defaultValue);
            return defaultValue;
        }
        else
        {
        	return val;
        }
    }
    
    public static void SaveToConfig(String data, Object value)
    {
    	mainPlugin.getConfig().set(data, value);
    	mainPlugin.saveConfig();
    }
    
    public static void ShowHelp(Player player)
    {
    	player.sendMessage(ChatColor.GREEN + "/openinv <Player> - Open a player's inventory");
    	player.sendMessage(ChatColor.GREEN + "   (aliases: oi, inv, open)");
    	player.sendMessage(ChatColor.GREEN + "/toggleopeninv - Toggle item openinv function");
    	player.sendMessage(ChatColor.GREEN + "   (aliases: toi, toggleoi, toggleinv)");
    	player.sendMessage(ChatColor.GREEN + "/searchinv <Item> [MinAmount] - ");
    	player.sendMessage(ChatColor.GREEN + "   Search and list players having a specific item.");
    	player.sendMessage(ChatColor.GREEN + "   (aliases: si, search)");
    	player.sendMessage(ChatColor.GREEN + "/anychest - Toggle anychest function");
    	player.sendMessage(ChatColor.GREEN + "   (aliases: ac)");
    	player.sendMessage(ChatColor.GREEN + "/silentchest - Toggle silent chest function");
    	player.sendMessage(ChatColor.GREEN + "   (aliases: sc, silent)");
    }
}