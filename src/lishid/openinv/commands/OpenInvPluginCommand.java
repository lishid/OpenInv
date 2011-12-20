package lishid.openinv.commands;

import java.util.HashMap;

import lishid.openinv.PermissionRelay;
import lishid.openinv.OpenInv;
import lishid.openinv.utils.PlayerInventoryChest;
import lishid.openinv.utils.OpenInvHistory;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class OpenInvPluginCommand implements CommandExecutor {
    private final OpenInv plugin;
    public static HashMap<PlayerInventoryChest, Player> offlineInv = new HashMap<PlayerInventoryChest, Player>();
    public static HashMap<Player, OpenInvHistory> theOpenInvHistory = new HashMap<Player, OpenInvHistory>();
    public OpenInvPluginCommand(OpenInv plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if (!PermissionRelay.hasPermission((Player) sender, "OpenInv.openinv")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to access player inventories");
            return true;
        }

    	boolean Offline = false;
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

		//Target selecting
		Player target;

		String name = "";

		if (args.length < 1) {
			if(history.lastPlayer != null)
			{
				name = history.lastPlayer;
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
				if(!this.plugin.getServer().getOfflinePlayer(name).hasPlayedBefore())
				{
					sender.sendMessage(ChatColor.RED + "Player not found!");
					return true;
				}
				MinecraftServer server = ((CraftServer)this.plugin.getServer()).getServer();
				EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), name, new ItemInWorldManager(server.getWorldServer(0)));
				target = (entity == null) ? null : (Player) entity.getBukkitEntity();
				if(target != null)
				{
					Offline = true;
					target.loadData();
					EntityPlayer entityplayer = ((CraftPlayer)target).getHandle();
			    	entityplayer.inventory = new PlayerInventoryChest(entityplayer.inventory, entityplayer);
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "Player not found!");
					return false;
				}
			}
			catch(Exception e)
			{
				sender.sendMessage("Error while retrieving offline player data!");
				e.printStackTrace();
				//sender.sendMessage(ChatColor.RED + "Player '" + args[0] + "' not found!");
				return true;
			}
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

		if(entitytarget.inventory instanceof PlayerInventoryChest)
		{
			((PlayerInventoryChest)entitytarget.inventory).Opener = player;
			((PlayerInventoryChest)entitytarget.inventory).Target = target;

			if(Offline)
			{
				((PlayerInventoryChest)entitytarget.inventory).Offline = true;
				offlineInv.put((PlayerInventoryChest) entitytarget.inventory, target);
			}
		}

		entityplayer.a(entitytarget.inventory);

		return true;
    }
}
