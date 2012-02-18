package lishid.openinv.commands;

import lishid.openinv.OpenInv;
import lishid.openinv.PermissionRelay;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleOpenInvPluginCommand implements CommandExecutor {
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if(!(sender instanceof Player))
    	{
            sender.sendMessage(ChatColor.RED + "You can't use this from the console.");
    		return true;
    	}
    	if (!PermissionRelay.hasPermission((Player)sender, "openinv")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to access player inventories");
            return true;
        }
    	
		Player player = (Player)sender;
		if(args.length > 0)
		{
			if(args[0].equalsIgnoreCase("check"))
			{
				if(OpenInv.GetPlayerItemOpenInvStatus(player.getName()))
					player.sendMessage("OpenInv with " + Material.getMaterial(OpenInv.GetItemOpenInvItem()).toString() + " is ON.");
				else
					player.sendMessage("OpenInv with " + Material.getMaterial(OpenInv.GetItemOpenInvItem()).toString() + " is OFF.");
			}
		}
		if(OpenInv.GetPlayerItemOpenInvStatus(player.getName()))
		{
			OpenInv.SetPlayerItemOpenInvStatus(player.getName(), false);
			player.sendMessage("OpenInv with " + Material.getMaterial(OpenInv.GetItemOpenInvItem()).toString() + " is OFF.");
		}
		else
		{
			OpenInv.SetPlayerItemOpenInvStatus(player.getName(), true);
			player.sendMessage("OpenInv with " + Material.getMaterial(OpenInv.GetItemOpenInvItem()).toString() + " is ON.");
		}
		return true;
    }
}
