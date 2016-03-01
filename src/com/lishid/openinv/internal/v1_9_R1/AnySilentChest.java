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

package com.lishid.openinv.internal.v1_9_R1;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.internal.IAnySilentChest;

//Volatile
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;

import net.minecraft.server.v1_9_R1.AxisAlignedBB;
import net.minecraft.server.v1_9_R1.Block;
import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.EntityOcelot;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.IInventory;
import net.minecraft.server.v1_9_R1.ITileInventory;
import net.minecraft.server.v1_9_R1.InventoryLargeChest;
import net.minecraft.server.v1_9_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_9_R1.TileEntityChest;
import net.minecraft.server.v1_9_R1.World;

public class AnySilentChest implements IAnySilentChest {
    @Override
    public boolean IsAnyChestNeeded(Player p, int x, int y, int z) {
        // FOR REFERENCE, LOOK AT net.minecraft.server.BlockChest
        EntityPlayer player = ((CraftPlayer) p).getHandle();
        World world = player.world;
        // If block or ocelot on top
        if (world.getType(new BlockPosition(x, y + 1, z)).l() || hasOcelotOnTop(world, x, y, z))
            return true;

        BlockPosition position = new BlockPosition(x, y, z);
        int id = Block.getId(world.getType(position).getBlock());

        // If block next to chest is chest and has a block or ocelot on top
        if (isBlockedChest(world, id, x - 1, y, z))
            return true;
        if (isBlockedChest(world, id, x + 1, y, z))
            return true;
        if (isBlockedChest(world, id, x, y, z - 1))
            return true;
        if (isBlockedChest(world, id, x, y, z + 1))
            return true;

        return false;
    }

    private boolean isBlockedChest(World world, int id, int x, int y, int z) {
        BlockPosition position = new BlockPosition(x, y, z);
        if (Block.getId(world.getType(position).getBlock()) != id) {
            return false;
        }

        if (world.getType(position).l()) {
            return true;
        }

        return hasOcelotOnTop(world, x, y, z);
    }

    private boolean hasOcelotOnTop(World world, int x, int y, int z) {
        for (Entity localEntity : world.a(EntityOcelot.class,
                new AxisAlignedBB(x, y + 1, z, x + 1, y + 2, z + 1))) {
            EntityOcelot localEntityOcelot = (EntityOcelot) localEntity;
            if (localEntityOcelot.isSitting()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean ActivateChest(Player p, boolean anychest, boolean silentchest, int x, int y, int z) {
        EntityPlayer player = ((CraftPlayer) p).getHandle();
        World world = player.world;
        Object chest = world.getTileEntity(new BlockPosition(x, y, z));
        if (chest == null)
            return true;

        int id = Block.getId(world.getType(new BlockPosition(x, y, z)).getBlock());

        if (!anychest) {
            if (world.getType(new BlockPosition(x, y + 1, z)).l())
                return true;
            if ((Block.getId(world.getType(new BlockPosition(x - 1, y, z)).getBlock()) == id) && (world.getType(new BlockPosition(x - 1, y + 1, z)).l()))
                return true;
            if ((Block.getId(world.getType(new BlockPosition(x + 1, y, z)).getBlock()) == id) && (world.getType(new BlockPosition(x + 1, y + 1, z)).l()))
                return true;
            if ((Block.getId(world.getType(new BlockPosition(x, y, z - 1)).getBlock()) == id) && (world.getType(new BlockPosition(x, y + 1, z - 1)).l()))
                return true;
            if ((Block.getId(world.getType(new BlockPosition(x, y, z + 1)).getBlock()) == id) && (world.getType(new BlockPosition(x, y + 1, z + 1)).l()))
                return true;
        }

        if (Block.getId(world.getType(new BlockPosition(x - 1, y, z)).getBlock()) == id)
            chest = new InventoryLargeChest("Large chest", (TileEntityChest) world.getTileEntity(new BlockPosition(x - 1, y, z)), (ITileInventory) chest);
        if (Block.getId(world.getType(new BlockPosition(x + 1, y, z)).getBlock()) == id)
            chest = new InventoryLargeChest("Large chest", (ITileInventory) chest, (TileEntityChest) world.getTileEntity(new BlockPosition(x + 1, y, z)));
        if (Block.getId(world.getType(new BlockPosition(x, y, z - 1)).getBlock()) == id)
            chest = new InventoryLargeChest("Large chest", (TileEntityChest) world.getTileEntity(new BlockPosition(x, y, z - 1)), (ITileInventory) chest);
        if (Block.getId(world.getType(new BlockPosition(x, y, z + 1)).getBlock()) == id)
            chest = new InventoryLargeChest("Large chest", (ITileInventory) chest, (TileEntityChest) world.getTileEntity(new BlockPosition(x, y, z + 1)));

        boolean returnValue = true;
        if (!silentchest) {
            player.openContainer((IInventory) chest);
        }
        else {
            try {
                SilentContainerChest silentContainerChest = new SilentContainerChest(player.inventory, ((IInventory) chest), player);
                int windowId = player.nextContainerCounter();
                player.playerConnection.sendPacket(new PacketPlayOutOpenWindow(windowId, "minecraft:chest", ((IInventory) chest).getScoreboardDisplayName(), ((IInventory) chest).getSize()));
                player.activeContainer = silentContainerChest;
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
