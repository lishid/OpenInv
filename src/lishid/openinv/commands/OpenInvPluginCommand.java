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
    public static HashMap<Player, OpenInvHistory> theOpenInvHistory = new HashMap<Player, OpenInvHistory>();
    public OpenInvPluginCommand(OpenInv plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if (!PermissionRelay.hasPermission((Player) sender, "OpenInv.openinv")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to access player inventories");
            return true;
        }
        
		Player player = (Player)sender;
		OpenInvHistory history = theOpenInvHistory.get(player);
		
		if(history == null)
		{
			history = new OpenInvHistory(player);
			theOpenInvHistory.put(player, history);
		}
		
		if(command.getName().equalsIgnoreCase("toggleopeninv"))
		{
			if(OpenInvToggleState.openInvState.get(player.getName()) != null && OpenInvToggleState.openInvState.get(player.getName()) == 1)
			{
				OpenInvToggleState.openInvState.put(player.getName(), 0);
				player.sendMessage("OpenInv with stick is OFF.");
			}
			else
			{
				OpenInvToggleState.openInvState.put(player.getName(), 1);
				player.sendMessage("OpenInv with stick is ON.");
			}
			return true;
		}

		Player target;
		
		if (args.length < 1) {
			if(history.lastPlayer != null)
			{
				target = this.plugin.getServer().getPlayer(history.lastPlayer);
				//EntityPlayer entply = new EntityPlayer(((CraftServer)this.plugin.getServer()).getServer(), ((CraftPlayer)player).getHandle().world, "", null);
				//CraftPlayer ply = new CraftPlayer((CraftServer) this.plugin.getServer(), null);
			}
			else
			{
				sender.sendMessage("OpenInv history is empty!");
				return false;
			}
		}
		else
		{
			target = this.plugin.getServer().getPlayer(args[0]);
		}


		if(target == null)
		{
			sender.sendMessage("Player not found!");
			return false;
		}
		if(target == player)
		{
			sender.sendMessage("Cannot target yourself!");
			return false;
		}
		
		if (!PermissionRelay.hasPermission(player, "OpenInv.override") && PermissionRelay.hasPermission(target, "OpenInv.exempt")) {
            sender.sendMessage(ChatColor.RED + target.getDisplayName() + "'s inventory is protected!");
            return true;
        }

		history.lastPlayer = target.getName();
		
		// Get the EntityPlayer handle from the sender
		EntityPlayer entityplayer = ((CraftPlayer) player).getHandle();

		// Get the EntityPlayer from the Target
		EntityPlayer entitytarget = ((CraftPlayer) target).getHandle();
		
		if(!(entitytarget.inventory instanceof PlayerInventoryChest))
		{
			OpenInv.ReplaceInv((CraftPlayer) target);
		}
		
		entityplayer.a(entitytarget.inventory);

		return true;
    }
}
