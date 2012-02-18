package lishid.openinv;

import java.lang.reflect.Field;

import lishid.openinv.commands.OpenInvPluginCommand;
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
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Event.Result;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class OpenInvPlayerListener implements Listener{
	OpenInv plugin;
	public OpenInvPlayerListener(OpenInv scrap) {
		plugin = scrap;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLogin(PlayerLoginEvent event)
	{
		try{
			for(Player target : OpenInvPluginCommand.offlineInv.keySet())
			{
				if(target.getName().equalsIgnoreCase(event.getPlayer().getName()))
				{
					((CraftPlayer)OpenInvPluginCommand.offlineInv.get(target).Opener).getHandle().netServerHandler.sendPacket(new Packet101CloseWindow());
					target.saveData();
					OpenInvPluginCommand.offlineInv.remove(target);
					event.getPlayer().loadData();
					return;
				}
			}
		}
		catch(Exception e){}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.useInteractedBlock() == Result.DENY)
			return;
		
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Chest)
		{
			boolean silentchest = false;
			boolean anychest = false;
			int x = event.getClickedBlock().getX();
			int y = event.getClickedBlock().getY();
			int z = event.getClickedBlock().getZ();
	
			if(PermissionRelay.hasPermission(event.getPlayer(), "silent") && OpenInv.GetPlayerSilentChestStatus(event.getPlayer().getName()))
			{
				silentchest = true;
			}
	
			if(PermissionRelay.hasPermission(event.getPlayer(), "anychest") && OpenInv.GetPlayerAnyChestStatus(event.getPlayer().getName()))
			{
				try
				{
					//FOR REFERENCE, LOOK AT net.minecraft.server.BlockChest
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
				catch(Exception e)
				{
					event.getPlayer().sendMessage(ChatColor.RED + "Error while executing openinv. Unsupported CraftBukkit.");
					e.printStackTrace();
				}
			}
			
		    //If the anychest or silentchest is active
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
			        	Field windowID;
			        	try{
			        		windowID = player.getClass().getDeclaredField("cl");
			        	}
			        	catch(NoSuchFieldException e)
			        	{
			        		windowID = player.getClass().getDeclaredField("ci");
			        	}
			        	windowID.setAccessible(true);
			        	int id = windowID.getInt(player);
			            id = id % 100 + 1;
			            windowID.setInt(player, id);
			            player.netServerHandler.sendPacket(new Packet100OpenWindow(id, 0, ((IInventory)chest).getName(), ((IInventory)chest).getSize()));
			            player.activeContainer = new SilentContainerChest(player.inventory, ((IInventory)chest));
			            player.activeContainer.windowId = id;
			            player.activeContainer.a((ICrafting)player);
			        	//event.getPlayer().sendMessage("You are opening a chest silently.");
			        	event.setUseInteractedBlock(Result.DENY);
			        	event.setCancelled(true);
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
		}
		
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign)
		{
			Player player = event.getPlayer();
			try{
				Sign sign = ((Sign)event.getClickedBlock().getState());
				if (PermissionRelay.hasPermission(player, "openinv") && sign.getLine(0).equalsIgnoreCase("[openinv]"))
				{
					String text = sign.getLine(1).trim() + sign.getLine(2).trim() + sign.getLine(3).trim();
					player.performCommand("openinv " + text);
				}
			}
			catch(Exception ex)
			{
				player.sendMessage("Internal Error.");
				ex.printStackTrace();
			}
		}
		
		if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			Player player = event.getPlayer();

			if(!(player.getItemInHand().getType().getId() == OpenInv.GetItemOpenInvItem())
    				|| (!OpenInv.GetPlayerItemOpenInvStatus(player.getName()))
					|| !PermissionRelay.hasPermission(player, "openinv"))
			{
				return;
			}
			
			player.performCommand("openinv");
		}
	}
}