package lishid.openinv.commands;

import lishid.openinv.PermissionRelay;
import lishid.openinv.OpenInv;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SearchInvPluginCommand implements CommandExecutor {
    private final OpenInv plugin;
    public SearchInvPluginCommand(OpenInv plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if (!PermissionRelay.hasPermission((Player) sender, "OpenInv.search")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to access player inventories");
            return true;
        }
        
    	;
    	
		String PlayerList = "";
		
        Material material = null;
        int count = 1;

        if (args.length >= 1) {
            String[] gData = null;
            gData = args[0].split(":");
            material = Material.matchMaterial(gData[0]);
        }
        if (args.length >= 2) {
            try {
                count = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                sender.sendMessage(ChatColor.RED + "'" + args[1] + "' is not a number!");
                return false;
            }
        }
		
		if (material == null) {
            sender.sendMessage(ChatColor.RED + "Unknown item");
            return false;
        }

		for(Player templayer : plugin.getServer().getOnlinePlayers())
		{
			if(templayer.getInventory().contains(material, count))
			{
				PlayerList += templayer.getName() + "  ";
			}
		}
		
        sender.sendMessage("Players with the item " + material.toString() + ":  " + PlayerList);
        return true;
    }
}
