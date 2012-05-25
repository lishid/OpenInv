/*
 * Copyright (C) 2011-2012 lishid.  All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package lishid.openinv;

import java.lang.reflect.Field;

import lishid.openinv.utils.SilentContainerChest;
import net.minecraft.server.Block;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.IInventory;
import net.minecraft.server.InventoryLargeChest;
import net.minecraft.server.Packet100OpenWindow;
import net.minecraft.server.TileEntityChest;
import net.minecraft.server.World;

import org.bukkit.ChatColor;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import balor.OpenInv.InventoryManager;

public class OpenInvPlayerListener implements Listener {
	OpenInv plugin;

	public OpenInvPlayerListener(OpenInv scrap) {
		plugin = scrap;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK
				&& event.useInteractedBlock() == Result.DENY)
			return;

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK
				&& event.getClickedBlock().getState() instanceof Chest) {
			boolean silentchest = false;
			boolean anychest = false;
			int x = event.getClickedBlock().getX();
			int y = event.getClickedBlock().getY();
			int z = event.getClickedBlock().getZ();

			if (event.getPlayer().hasPermission("OpenInv.silent")
					&& OpenInv.GetPlayerSilentChestStatus(event.getPlayer().getName())) {
				silentchest = true;
			}

			if (event.getPlayer().hasPermission("OpenInv.anychest")
					&& OpenInv.GetPlayerAnyChestStatus(event.getPlayer().getName())) {
				try {
					// FOR REFERENCE, LOOK AT net.minecraft.server.BlockChest
					EntityPlayer player = ((CraftPlayer) event.getPlayer()).getHandle();
					World world = player.world;
					// If block on top
					if (world.e(x, y + 1, z))
						anychest = true;

					// If block next to chest is chest and has a block on top
					if ((world.getTypeId(x - 1, y, z) == Block.CHEST.id)
							&& (world.e(x - 1, y + 1, z)))
						anychest = true;
					if ((world.getTypeId(x + 1, y, z) == Block.CHEST.id)
							&& (world.e(x + 1, y + 1, z)))
						anychest = true;
					if ((world.getTypeId(x, y, z - 1) == Block.CHEST.id)
							&& (world.e(x, y + 1, z - 1)))
						anychest = true;
					if ((world.getTypeId(x, y, z + 1) == Block.CHEST.id)
							&& (world.e(x, y + 1, z + 1)))
						anychest = true;
				} catch (Exception e) {
					event.getPlayer().sendMessage(
							ChatColor.RED
									+ "Error while executing openinv. Unsupported CraftBukkit.");
					e.printStackTrace();
				}
			}

			// If the anychest or silentchest is active
			if (anychest || silentchest) {
				EntityPlayer player = ((CraftPlayer) event.getPlayer()).getHandle();
				World world = player.world;
				Object chest = (TileEntityChest) world.getTileEntity(x, y, z);
				if (chest == null)
					return;

				if (!anychest) {
					if (world.e(x, y + 1, z))
						return;
					if ((world.getTypeId(x - 1, y, z) == Block.CHEST.id)
							&& (world.e(x - 1, y + 1, z)))
						return;
					if ((world.getTypeId(x + 1, y, z) == Block.CHEST.id)
							&& (world.e(x + 1, y + 1, z)))
						return;
					if ((world.getTypeId(x, y, z - 1) == Block.CHEST.id)
							&& (world.e(x, y + 1, z - 1)))
						return;
					if ((world.getTypeId(x, y, z + 1) == Block.CHEST.id)
							&& (world.e(x, y + 1, z + 1)))
						return;
				}

				if (world.getTypeId(x - 1, y, z) == Block.CHEST.id)
					chest = new InventoryLargeChest("Large chest",
							(TileEntityChest) world.getTileEntity(x - 1, y, z), (IInventory) chest);
				if (world.getTypeId(x + 1, y, z) == Block.CHEST.id)
					chest = new InventoryLargeChest("Large chest", (IInventory) chest,
							(TileEntityChest) world.getTileEntity(x + 1, y, z));
				if (world.getTypeId(x, y, z - 1) == Block.CHEST.id)
					chest = new InventoryLargeChest("Large chest",
							(TileEntityChest) world.getTileEntity(x, y, z - 1), (IInventory) chest);
				if (world.getTypeId(x, y, z + 1) == Block.CHEST.id)
					chest = new InventoryLargeChest("Large chest", (IInventory) chest,
							(TileEntityChest) world.getTileEntity(x, y, z + 1));

				if (!silentchest) {
					player.openContainer((IInventory) chest);
				} else {
					try {
						int id = 0;
						try {
							Field windowID = player.getClass().getDeclaredField("containerCounter");
							windowID.setAccessible(true);
							id = windowID.getInt(player);
							id = id % 100 + 1;
							windowID.setInt(player, id);
						} catch (NoSuchFieldException e) {
						}

						player.netServerHandler.sendPacket(new Packet100OpenWindow(id, 0,
								((IInventory) chest).getName(), ((IInventory) chest).getSize()));
						player.activeContainer = new SilentContainerChest(player.inventory,
								((IInventory) chest));
						player.activeContainer.windowId = id;
						player.activeContainer.addSlotListener(player);
						// event.getPlayer().sendMessage("You are opening a chest silently.");
						event.setUseInteractedBlock(Result.DENY);
						event.setCancelled(true);
					} catch (Exception e) {
						e.printStackTrace();
						event.getPlayer().sendMessage(
								ChatColor.RED + "Error while sending silent chest.");
					}
				}

				if (anychest)
					event.getPlayer().sendMessage("You are opening a blocked chest.");
			}
		}

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK
				&& event.getClickedBlock().getState() instanceof Sign) {
			Player player = event.getPlayer();
			try {
				Sign sign = ((Sign) event.getClickedBlock().getState());
				if (player.hasPermission("OpenInv.openinv")
						&& sign.getLine(0).equalsIgnoreCase("[openinv]")) {
					String text = sign.getLine(1).trim() + sign.getLine(2).trim()
							+ sign.getLine(3).trim();
					player.performCommand("openinv " + text);
				}
			} catch (Exception ex) {
				player.sendMessage("Internal Error.");
				ex.printStackTrace();
			}
		}

		if (event.getAction() == Action.RIGHT_CLICK_AIR
				|| event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Player player = event.getPlayer();

			if (!(player.getItemInHand().getType().getId() == OpenInv.GetItemOpenInvItem())
					|| (!OpenInv.GetPlayerItemOpenInvStatus(player.getName()))
					|| !player.hasPermission("OpenInv.openinv")) {
				return;
			}

			player.performCommand("openinv");
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		InventoryManager.INSTANCE.onQuit(event.getPlayer());
	}
}