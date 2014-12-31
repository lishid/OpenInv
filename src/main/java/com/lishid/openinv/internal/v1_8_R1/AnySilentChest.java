/*
 * Copyright (C) 2011-2014 lishid.  All rights reserved.
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

package com.lishid.openinv.internal.v1_8_R1;

import java.lang.reflect.Field;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.internal.IAnySilentChest;


//Volatile
import net.minecraft.server.v1_8_R1.*;

import org.bukkit.craftbukkit.v1_8_R1.entity.*;

public class AnySilentChest implements IAnySilentChest {
    public boolean IsAnyChestNeeded(Player p, int x, int y, int z) {
        // FOR REFERENCE, LOOK AT net.minecraft.server.BlockChest
        EntityPlayer player = ((CraftPlayer) p).getHandle();
        World world = player.world;

        // If block on top
        if (world.getType(new BlockPosition(x, y + 1, z)).getBlock().c()){
            return true;
        }

        int id = Block.getId(world.getType(new BlockPosition(x, y, z)).getBlock());

        // If block next to chest is chest and has a block on top
        if ((Block.getId(world.getType(new BlockPosition(x - 1, y, z)).getBlock()) == id) && (world.getType(new BlockPosition(x - 1, y + 1, z)).getBlock().c()))
            return true;
        if ((Block.getId(world.getType(new BlockPosition(x + 1, y, z)).getBlock()) == id) && (world.getType(new BlockPosition(x + 1, y + 1, z)).getBlock().c()))
            return true;
        if ((Block.getId(world.getType(new BlockPosition(x, y, z - 1)).getBlock()) == id) && (world.getType(new BlockPosition(x, y + 1, z - 1)).getBlock().c()))
            return true;
        if ((Block.getId(world.getType(new BlockPosition(x, y, z + 1)).getBlock()) == id) && (world.getType(new BlockPosition(x, y + 1, z + 1)).getBlock().c()))
            return true;

        return false;
    }

    public boolean ActivateChest(Player p, boolean anychest, boolean silentchest, int x, int y, int z) {
        EntityPlayer player = ((CraftPlayer) p).getHandle();
        World world = player.world;
        Object chest = (TileEntityChest) world.getTileEntity(new BlockPosition(x, y, z));
        if (chest == null)
            return true;

        int id = Block.getId(world.getType(new BlockPosition(x, y, z)).getBlock());

        if (!anychest) {
            if (world.getType(new BlockPosition(x, y + 1, z)).getBlock().c())
                return true;
            if ((Block.getId(world.getType(new BlockPosition(x - 1, y, z)).getBlock()) == id) && (world.getType(new BlockPosition(x - 1, y + 1, z)).getBlock().c()))
                return true;
            if ((Block.getId(world.getType(new BlockPosition(x + 1, y, z)).getBlock()) == id) && (world.getType(new BlockPosition(x + 1, y + 1, z)).getBlock().c()))
                return true;
            if ((Block.getId(world.getType(new BlockPosition(x, y, z - 1)).getBlock()) == id) && (world.getType(new BlockPosition(x, y + 1, z - 1)).getBlock().c()))
                return true;
            if ((Block.getId(world.getType(new BlockPosition(x, y, z + 1)).getBlock()) == id) && (world.getType(new BlockPosition(x, y + 1, z + 1)).getBlock().c()))
                return true;
        }

        if (Block.getId(world.getType(new BlockPosition(x - 1, y, z)).getBlock()) == id)
        	chest = new InventoryLargeChest("Large chest", (ITileInventory) world.getTileEntity(new BlockPosition(x - 1, y, z)), (ITileInventory) chest);
        if (Block.getId(world.getType(new BlockPosition(x + 1, y, z)).getBlock()) == id)
            chest = new InventoryLargeChest("Large chest", (ITileInventory) chest, (ITileInventory) world.getTileEntity(new BlockPosition(x + 1, y, z)));
        if (Block.getId(world.getType(new BlockPosition(x, y, z - 1)).getBlock()) == id)
            chest = new InventoryLargeChest("Large chest", (ITileInventory) world.getTileEntity(new BlockPosition(x, y, z - 1)), (ITileInventory) chest);
        if (Block.getId(world.getType(new BlockPosition(x, y, z + 1)).getBlock()) == id)
            chest = new InventoryLargeChest("Large chest", (ITileInventory) chest, (ITileInventory) world.getTileEntity(new BlockPosition(x, y, z + 1)));

        boolean returnValue = true;
        if (!silentchest) {
            player.openContainer((IInventory) chest);
        }
        else {
            try {
                int windowId = 0;
                try {
                    Field windowID = player.getClass().getDeclaredField("containerCounter");
                    windowID.setAccessible(true);
                    windowId = windowID.getInt(player);
                    windowId = windowId % 100 + 1;
                    windowID.setInt(player, windowId);
                }
                catch (NoSuchFieldException e) {}
                player.playerConnection.sendPacket(new PacketPlayOutOpenWindow(windowId, "minecraft:chest", new ChatComponentText(((IInventory) chest).getName()), ((IInventory) chest).getSize()));
                player.activeContainer = new SilentContainerChest(player.inventory, ((IInventory) chest), player);
                player.activeContainer.windowId = windowId;
                player.activeContainer.addSlotListener(player);
                if (OpenInv.NotifySilentChest()) {
                    p.sendMessage("You are opening a chest silently.");
                }
                returnValue = false;
            }
            catch (Exception e) {
                e.printStackTrace();
                p.sendMessage(ChatColor.RED + "Error while sending silent chest.");
            }
        }

        if (anychest && OpenInv.NotifyAnyChest()) {
            p.sendMessage("You are opening a blocked chest.");
        }

        return returnValue;
    }
}
