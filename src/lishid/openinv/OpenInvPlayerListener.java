package lishid.openinv;

import net.minecraft.server.Block;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.IInventory;
import net.minecraft.server.InventoryLargeChest;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class OpenInvPlayerListener extends PlayerListener{
	OpenInv plugin;
	public OpenInvPlayerListener(OpenInv scrap) {
		plugin = scrap;
	}

	@Override
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		OpenInv.ReplaceInv((CraftPlayer) event.getPlayer());
	}

	@Override
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		OpenInv.ReplaceInv((CraftPlayer) event.getPlayer());
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(event.useInteractedBlock() == Result.DENY)
			return;
		
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && 
				event.getClickedBlock().getState() instanceof Chest && 
				PermissionRelay.hasPermission(event.getPlayer(), "OpenInv.anychest"))
		{
			EntityPlayer player = ((CraftPlayer)event.getPlayer()).getHandle();
			World world = player.world;
			int x = event.getClickedBlock().getX();
			int y = event.getClickedBlock().getY();
			int z = event.getClickedBlock().getZ();
			try
			{
				boolean override = false;
				
				//If block on top
				if(world.e(x, y + 1, z))
					override = true;
	
				//If block next to chest is chest and has a block on top
			    if ((world.getTypeId(x - 1, y, z) == Block.CHEST.id) && (world.e(x - 1, y + 1, z)))
			    	override = true;
			    if ((world.getTypeId(x + 1, y, z) == Block.CHEST.id) && (world.e(x + 1, y + 1, z)))
			    	override = true;
			    if ((world.getTypeId(x, y, z - 1) == Block.CHEST.id) && (world.e(x, y + 1, z - 1)))
			    	override = true;
			    if ((world.getTypeId(x, y, z + 1) == Block.CHEST.id) && (world.e(x, y + 1, z + 1)))
			    	override = true;
				
			    //If the chest is blocked
			    if(override)
			    {
			    	//Create chest
					Object inventory = (TileEntityChest)player.world.getTileEntity(x, y, z);
				    
					//Link chest
				    if (world.getTypeId(x - 1, y, z) == Block.CHEST.id) inventory = new InventoryLargeChest("Large chest", (TileEntityChest)world.getTileEntity(x - 1, y, z), (IInventory)inventory);
				    if (world.getTypeId(x + 1, y, z) == Block.CHEST.id) inventory = new InventoryLargeChest("Large chest", (IInventory)inventory, (TileEntityChest)world.getTileEntity(x + 1, y, z));
				    if (world.getTypeId(x, y, z - 1) == Block.CHEST.id) inventory = new InventoryLargeChest("Large chest", (TileEntityChest)world.getTileEntity(x, y, z - 1), (IInventory)inventory);
				    if (world.getTypeId(x, y, z + 1) == Block.CHEST.id) inventory = new InventoryLargeChest("Large chest", (IInventory)inventory, (TileEntityChest)world.getTileEntity(x, y, z + 1));
	
				    //Open chest
				    player.a((IInventory)inventory);
				    
				    //Send a notification
				    event.getPlayer().sendMessage("You are opening a blocked chest.");
			    }
			    /*
				Chest chest = (Chest)event.getClickedBlock().getState();
				player.a(((CraftInventory)chest.getInventory()).getInventory());*/
				return;
			}
			catch(Exception e) //Incompatible CraftBukkit?
			{
				e.printStackTrace();
				event.getPlayer().sendMessage(ChatColor.RED + "Error while executing openinv. Unsupported CraftBukkit.");
			}
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
						((Sign)event.getClickedBlock().getState()).getLine(1).equalsIgnoreCase("[openinv]"))
				{
					if(plugin.getServer().getPlayer(((Sign)event.getClickedBlock().getState()).getLine(2)) != null)
					{
						player.performCommand("openinv " + ((Sign)event.getClickedBlock().getState()).getLine(2));
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
			}
		}
	}
}
