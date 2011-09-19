package lishid.openinv;

import net.minecraft.server.Block;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
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
		if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			if(event.getClickedBlock() == Block.CHEST ||
					event.getClickedBlock() == Block.FURNACE ||
					event.getClickedBlock() == Block.DISPENSER)
			{
				return;
			}
			
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
