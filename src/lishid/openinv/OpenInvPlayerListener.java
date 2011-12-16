package lishid.openinv;

import java.lang.reflect.Field;

import lishid.openinv.commands.OpenInvPluginCommand;
import lishid.openinv.utils.PlayerInventoryChest;
import lishid.openinv.utils.SilentContainerChest;
import net.minecraft.server.Block;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ICrafting;
import net.minecraft.server.IInventory;
import net.minecraft.server.InventoryLargeChest;
import net.minecraft.server.Packet100OpenWindow;
import net.minecraft.server.Packet101CloseWindow;
import net.minecraft.server.TileEntityChest;
import net.minecraft.server.World;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;

public class OpenInvPlayerListener extends PlayerListener{
	OpenInv plugin;
	public OpenInvPlayerListener(OpenInv scrap) {
		plugin = scrap;
	}

	@Override
	public void onPlayerLogin(PlayerLoginEvent event)
	{
		try{
			for(Player target : OpenInvPluginCommand.offlineInv.values())
			{
				if(target.getName().equalsIgnoreCase(event.getPlayer().getName()))
				{
					System.out.print("[OpenInv] PlayerLogin event triggered closing openinv.");
					EntityPlayer player = ((CraftPlayer)target).getHandle();
					if(player.inventory instanceof PlayerInventoryChest)
					{
						((CraftPlayer)((PlayerInventoryChest)player.inventory).Opener).getHandle().netServerHandler.sendPacket(new Packet101CloseWindow());
					}
					target.saveData();
					OpenInvPluginCommand.offlineInv.remove(player.inventory);
					event.getPlayer().loadData();
					break;
				}
			}
		}
		catch(Exception e){}
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(event.useInteractedBlock() == Result.DENY || event.isCancelled())
			return;
		
		boolean silentchest = false;
		boolean anychest = false;
		int x = event.getClickedBlock().getX();
		int y = event.getClickedBlock().getY();
		int z = event.getClickedBlock().getZ();

		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && 
				event.getClickedBlock().getState() instanceof Chest && 
				PermissionRelay.hasPermission(event.getPlayer(), "OpenInv.silent") &&
				OpenInv.GetPlayerSilentChestStatus(event.getPlayer().getName()))
		{
			silentchest = true;
		}

		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && 
				event.getClickedBlock().getState() instanceof Chest && 
				PermissionRelay.hasPermission(event.getPlayer(), "OpenInv.anychest"))
		{
			try
			{
				EntityPlayer player = ((CraftPlayer)event.getPlayer()).getHandle();
				World world = player.world;
				//If block on top
				if(world.e(x, y + 1, z))
					anychest = true;
	
				//If block next to chest is chest and has a block on top
			    if ((world.getTypeId(x - 1, y, z) == Block.CHEST.id) && (world.e(x - 1, y + 1, z)))
			    	anychest = true;
			    if ((world.getTypeId(x + 1, y, z) == Block.CHEST.id) && (world.e(x + 1, y + 1, z)))
			    	anychest = true;
			    if ((world.getTypeId(x, y, z - 1) == Block.CHEST.id) && (world.e(x, y + 1, z - 1)))
			    	anychest = true;
			    if ((world.getTypeId(x, y, z + 1) == Block.CHEST.id) && (world.e(x, y + 1, z + 1)))
			    	anychest = true;
			}
			catch(Exception e) //Incompatible CraftBukkit?
			{
				e.printStackTrace();
				event.getPlayer().sendMessage(ChatColor.RED + "Error while executing openinv. Unsupported CraftBukkit.");
			}
		}
		

	    //If the chest is blocked
	    if(anychest || silentchest)
	    {
			EntityPlayer player = ((CraftPlayer)event.getPlayer()).getHandle();
			World world = player.world;
	    	Object chest = (TileEntityChest)world.getTileEntity(x, y, z);
	        if (chest == null) return;
	        
	        if(!anychest)
	        {
		        if (world.e(x, y + 1, z)) return;
		        if ((world.getTypeId(x - 1, y, z) == Block.CHEST.id) && (world.e(x - 1, y + 1, z))) return;
		        if ((world.getTypeId(x + 1, y, z) == Block.CHEST.id) && (world.e(x + 1, y + 1, z))) return;
		        if ((world.getTypeId(x, y, z - 1) == Block.CHEST.id) && (world.e(x, y + 1, z - 1))) return;
		        if ((world.getTypeId(x, y, z + 1) == Block.CHEST.id) && (world.e(x, y + 1, z + 1))) return;
	        }

	        if (world.getTypeId(x - 1, y, z) == Block.CHEST.id) chest = new InventoryLargeChest("Large chest", (TileEntityChest)world.getTileEntity(x - 1, y, z), (IInventory)chest);
	        if (world.getTypeId(x + 1, y, z) == Block.CHEST.id) chest = new InventoryLargeChest("Large chest", (IInventory)chest, (TileEntityChest)world.getTileEntity(x + 1, y, z));
	        if (world.getTypeId(x, y, z - 1) == Block.CHEST.id) chest = new InventoryLargeChest("Large chest", (TileEntityChest)world.getTileEntity(x, y, z - 1), (IInventory)chest);
	        if (world.getTypeId(x, y, z + 1) == Block.CHEST.id) chest = new InventoryLargeChest("Large chest", (IInventory)chest, (TileEntityChest)world.getTileEntity(x, y, z + 1));
	        
	        if(!silentchest)
	        {
	        	player.a((IInventory)chest);
	        }
	        else
	        {
	        	try{
		        	Field ciField = player.getClass().getDeclaredField("ci");
		        	ciField.setAccessible(true);
		        	int ci = ciField.getInt(player);
		            ci = ci % 100 + 1;
		            ciField.setInt(player, ci);
		            player.netServerHandler.sendPacket(new Packet100OpenWindow(ci, 0, ((IInventory)chest).getName(), ((IInventory)chest).getSize()));
		            player.activeContainer = new SilentContainerChest(player.inventory, ((IInventory)chest));
		        	System.out.println(player.activeContainer.toString());
		            player.activeContainer.windowId = ci;
		            player.activeContainer.a((ICrafting)player);
		        	event.getPlayer().sendMessage("You are opening a silent chest.");
	        	}
	        	catch(Exception e)
	        	{
					e.printStackTrace();
					event.getPlayer().sendMessage(ChatColor.RED + "Error while sending silent chest.");
				}
	        }

	        if(anychest)
	        	event.getPlayer().sendMessage("You are opening a blocked chest.");
	    }

		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && 
				(event.getClickedBlock() == Block.CHEST ||
				event.getClickedBlock() == Block.FURNACE ||
				event.getClickedBlock() == Block.DISPENSER))
		{
			return;
		}
		
		if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			Player player = event.getPlayer();
			
			if(!(player.getItemInHand().getType() == Material.STICK)
    				|| (!OpenInv.GetPlayerItemOpenInvStatus(player.getName()))
					|| !PermissionRelay.hasPermission(player, "openinv"))
			{
				return;
			}
			
			player.performCommand("openinv");
		}
		
		if(event.getAction() == Action.LEFT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign)
		{
			Player player = event.getPlayer();
			try{
				if (PermissionRelay.hasPermission(player, "openinv") &&
						((Sign)event.getClickedBlock().getState()).getLine(0).equalsIgnoreCase("[openinv]"))
				{
					if(plugin.getServer().getPlayer(((Sign)event.getClickedBlock().getState()).getLine(1)) != null)
					{
						Sign sign = ((Sign)event.getClickedBlock().getState());
						String text = sign.getLine(1).trim() + sign.getLine(2).trim() + sign.getLine(2).trim();
						player.performCommand("openinv " + text);
					}
					else
					{
						player.sendMessage("Player not found.");
					}
				}
			}
			catch(Exception ex)
			{
				player.sendMessage("Internal Error.");
				ex.printStackTrace();
			}
		}
	}
}
