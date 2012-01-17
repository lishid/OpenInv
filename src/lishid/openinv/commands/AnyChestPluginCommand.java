package lishid.openinv.commands;

import lishid.openinv.PermissionRelay;
import lishid.openinv.OpenInv;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AnyChestPluginCommand implements CommandExecutor {
    public AnyChestPluginCommand(OpenInv plugin) {
    	
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if (!PermissionRelay.hasPermission((Player) sender, "anychest")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use anychest.");
            return true;
        }
    	
    	if(args.length > 0)
		{
			if(args[0].equalsIgnoreCase("check"))
			{
				if(OpenInv.GetPlayerAnyChestStatus(sender.getName()))
					sender.sendMessage("AnyChest is ON.");
				else
					sender.sendMessage("AnyChest is OFF.");
			}
		}
    	
    	OpenInv.SetPlayerAnyChestStatus(sender.getName(), !OpenInv.GetPlayerAnyChestStatus(sender.getName()));
        sender.sendMessage("AnyChest is now " + (OpenInv.GetPlayerAnyChestStatus(sender.getName())?"On":"Off") + ".");
    	
        return true;
    }
}
