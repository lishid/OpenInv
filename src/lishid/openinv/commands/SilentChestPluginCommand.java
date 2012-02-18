package lishid.openinv.commands;

import lishid.openinv.PermissionRelay;
import lishid.openinv.OpenInv;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SilentChestPluginCommand implements CommandExecutor {
    public SilentChestPluginCommand(OpenInv plugin) {
    	
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if(!(sender instanceof Player))
    	{
            sender.sendMessage(ChatColor.RED + "You can't use this from the console.");
    		return true;
    	}
    	if (!PermissionRelay.hasPermission((Player) sender, "silent")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use silent chest.");
            return true;
        }
    	
    	if(args.length > 0)
		{
			if(args[0].equalsIgnoreCase("check"))
			{
				if(OpenInv.GetPlayerSilentChestStatus(sender.getName()))
					sender.sendMessage("SilentChest is ON.");
				else
					sender.sendMessage("SilentChest is OFF.");
			}
		}
    	
    	OpenInv.SetPlayerSilentChestStatus(sender.getName(), !OpenInv.GetPlayerSilentChestStatus(sender.getName()));
        sender.sendMessage("SilentChest is now " + (OpenInv.GetPlayerSilentChestStatus(sender.getName())?"On":"Off") + ".");
    	
        return true;
    }
}
