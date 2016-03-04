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

package com.lishid.openinv.internal.v1_4_5;

import java.lang.reflect.Field;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.lishid.openinv.internal.IAnySilentChest;

// Volatile
import net.minecraft.server.v1_4_5.Block;
import net.minecraft.server.v1_4_5.EntityPlayer;
import net.minecraft.server.v1_4_5.IInventory;
import net.minecraft.server.v1_4_5.InventoryLargeChest;
import net.minecraft.server.v1_4_5.Packet100OpenWindow;
import net.minecraft.server.v1_4_5.TileEntityChest;
import net.minecraft.server.v1_4_5.World;

import org.bukkit.craftbukkit.v1_4_5.entity.CraftPlayer;

public class AnySilentChest implements IAnySilentChest {
    @Override
    public boolean isAnyChestNeeded(Player p, int x, int y, int z) {
        // FOR REFERENCE, LOOK AT net.minecraft.server.BlockChest
        EntityPlayer player = ((CraftPlayer) p).getHandle();
        World world = player.world;
        // If block on top
        if (world.s(x, y + 1, z))
            return true;

        // If block next to chest is chest and has a block on top
        if ((world.getTypeId(x - 1, y, z) == Block.CHEST.id) && (world.s(x - 1, y + 1, z)))
            return true;
        if ((world.getTypeId(x + 1, y, z) == Block.CHEST.id) && (world.s(x + 1, y + 1, z)))
            return true;
        if ((world.getTypeId(x, y, z - 1) == Block.CHEST.id) && (world.s(x, y + 1, z - 1)))
            return true;
        if ((world.getTypeId(x, y, z + 1) == Block.CHEST.id) && (world.s(x, y + 1, z + 1)))
            return true;

        return false;
    }

    @Override
    public boolean activateChest(Player p, boolean anychest, boolean silentchest, int x, int y, int z) {
        EntityPlayer player = ((CraftPlayer) p).getHandle();
        World world = player.world;
        Object chest = world.getTileEntity(x, y, z);
        if (chest == null)
            return true;

        if (!anychest) {
            if (world.s(x, y + 1, z))
                return true;
            if ((world.getTypeId(x - 1, y, z) == Block.CHEST.id) && (world.s(x - 1, y + 1, z)))
                return true;
            if ((world.getTypeId(x + 1, y, z) == Block.CHEST.id) && (world.s(x + 1, y + 1, z)))
                return true;
            if ((world.getTypeId(x, y, z - 1) == Block.CHEST.id) && (world.s(x, y + 1, z - 1)))
                return true;
            if ((world.getTypeId(x, y, z + 1) == Block.CHEST.id) && (world.s(x, y + 1, z + 1)))
                return true;
        }

        if (world.getTypeId(x - 1, y, z) == Block.CHEST.id)
            chest = new InventoryLargeChest("Large chest", (TileEntityChest) world.getTileEntity(x - 1, y, z), (IInventory) chest);
        if (world.getTypeId(x + 1, y, z) == Block.CHEST.id)
            chest = new InventoryLargeChest("Large chest", (IInventory) chest, (TileEntityChest) world.getTileEntity(x + 1, y, z));
        if (world.getTypeId(x, y, z - 1) == Block.CHEST.id)
            chest = new InventoryLargeChest("Large chest", (TileEntityChest) world.getTileEntity(x, y, z - 1), (IInventory) chest);
        if (world.getTypeId(x, y, z + 1) == Block.CHEST.id)
            chest = new InventoryLargeChest("Large chest", (IInventory) chest, (TileEntityChest) world.getTileEntity(x, y, z + 1));

        boolean returnValue = true;
        if (!silentchest) {
            player.openContainer((IInventory) chest);
        }
        else {
            try {
                int id = 0;
                try {
                    Field windowID = player.getClass().getDeclaredField("containerCounter");
                    windowID.setAccessible(true);
                    id = windowID.getInt(player);
                    id = id % 100 + 1;
                    windowID.setInt(player, id);
                }
                catch (NoSuchFieldException e) {}

                player.netServerHandler.sendPacket(new Packet100OpenWindow(id, 0, ((IInventory) chest).getName(), ((IInventory) chest).getSize()));
                player.activeContainer = new SilentContainerChest(player.inventory, ((IInventory) chest));
                player.activeContainer.windowId = id;
                player.activeContainer.addSlotListener(player);
                returnValue = false;
            }
            catch (Exception e) {
                e.printStackTrace();
                p.sendMessage(ChatColor.RED + "Error while sending silent chest.");
            }
        }

        return returnValue;
    }
}
