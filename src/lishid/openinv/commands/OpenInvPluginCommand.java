package lishid.openinv.commands;

import java.util.HashMap;

import lishid.openinv.PermissionRelay;
import lishid.openinv.OpenInv;
import lishid.openinv.utils.OpenInvToggleState;
import lishid.openinv.utils.PlayerInventoryChest;
import lishid.openinv.utils.OpenInvHistory;

import net.minecraft.server.EntityPlayer;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class OpenInvPluginCommand implements CommandExecutor {
    private final OpenInv plugin;
    //public static HashMap<Player, PlayerInventoryChest> offlineInv = new HashMap<Player, PlayerInventoryChest>();
    public static HashMap<Player, OpenInvHistory> theOpenInvHistory = new HashMap<Player, OpenInvHistory>();
    public OpenInvPluginCommand(OpenInv plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if (!PermissionRelay.hasPermission((Player) sender, "OpenInv.openinv")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to access player inventories");
            return true;
        }
        
    	//boolean Offline = false;
		Player player = (Player)sender;
		
		//History management
		OpenInvHistory history = theOpenInvHistory.get(player);
		
		if(history == null)
		{
			history = new OpenInvHistory(player);
			theOpenInvHistory.put(player, history);
		}
		
		//Toggleopeninv command
		if(command.getName().equalsIgnoreCase("toggleopeninv"))
		{
			if(args.length > 0)
			{
				if(args[0].equalsIgnoreCase("check"))
				{
					if(OpenInvToggleState.openInvState.containsKey(player.getName()))
						player.sendMessage("OpenInv with stick is ON.");
					else
						player.sendMessage("OpenInv with stick is OFF.");
				}
			}
			if(OpenInvToggleState.openInvState.containsKey(player.getName()))
			{
				OpenInvToggleState.openInvState.remove(player.getName());
				player.sendMessage("OpenInv with stick is OFF.");
			}
			else
			{
				OpenInvToggleState.openInvState.put(player.getName(), 1);
				player.sendMessage("OpenInv with stick is ON.");
			}
			return true;
		}

		//Target selecting
		Player target;
		
		if (args.length < 1) {
			if(history.lastPlayer != null)
			{
				target = this.plugin.getServer().getPlayer(history.lastPlayer);
			}
			else
			{
				sender.sendMessage(ChatColor.RED + "OpenInv history is empty!");
				return true;
			}
		}
		else
		{
			target = this.plugin.getServer().getPlayer(args[0]);
		}


		if(target == null)
		{
			//Offline inv here...
			/*try{
				MinecraftServer server = ((CraftServer)this.plugin.getServer()).getServer();
				EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), args[0], new ItemInWorldManager(server.getWorldServer(0)));
				target = (entity == null) ? null : (Player) entity.getBukkitEntity();
				if(target != null)
				{
					Offline = true;
					target.loadData();
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "Player not found!");
					return false;
				}
			} catch(Exception e)
			{*/
				//sender.sendMessage("Error while retrieving offline player data!");
				sender.sendMessage(ChatColor.RED + "Player '" + args[0] + "' not found!");
				return true;
			/*}*/
		}
		
		//Check if target is the player him/her self
		if(target == player)
		{
			sender.sendMessage(ChatColor.RED + "Cannot OpenInv yourself!");
			return true;
		}
		
		//Permissions checks
		if (!PermissionRelay.hasPermission(player, "OpenInv.override") && PermissionRelay.hasPermission(target, "OpenInv.exempt")) {
            sender.sendMessage(ChatColor.RED + target.getDisplayName() + "'s inventory is protected!");
            return true;
        }
		
		if((!PermissionRelay.hasPermission(player, "OpenInv.crossworld") && !PermissionRelay.hasPermission(player, "OpenInv.override")) && 
				target.getWorld() != player.getWorld()){
			sender.sendMessage(ChatColor.RED + target.getDisplayName() + " is not in your world!");
            return true;
		}

		//The actual openinv
		history.lastPlayer = target.getName();
		
		// Get the EntityPlayer handle from the sender
		EntityPlayer entityplayer = ((CraftPlayer) player).getHandle();

		// Get the EntityPlayer from the Target
		EntityPlayer entitytarget = ((CraftPlayer) target).getHandle();
		
		if(!(entitytarget.inventory instanceof PlayerInventoryChest))
		{
			OpenInv.ReplaceInv((CraftPlayer) target);
		}
		/*
		if(Offline && entitytarget.inventory instanceof PlayerInventoryChest)
		{
			offlineInv.put(target, (PlayerInventoryChest) entitytarget.inventory);
		}
		*/
		entityplayer.a(entitytarget.inventory);

		return true;
    }
}
