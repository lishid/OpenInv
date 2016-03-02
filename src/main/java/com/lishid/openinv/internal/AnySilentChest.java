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

package com.lishid.openinv.internal;

import java.util.Iterator;

import org.bukkit.entity.Player;

import com.lishid.openinv.OpenInv;

// Volatile
import net.minecraft.server.v1_9_R1.*;
import net.minecraft.server.v1_9_R1.BlockChest.Type;

import org.bukkit.craftbukkit.v1_9_R1.entity.*;

public class AnySilentChest {
    public boolean isAnyChestNeeded(Player p, int x, int y, int z) {
        // FOR REFERENCE, LOOK AT net.minecraft.server.BlockChest
        BlockPosition position = new BlockPosition(x, y, z);
        EntityPlayer player = ((CraftPlayer) p).getHandle();
        World world = player.world;
        BlockChest chest = (BlockChest) (((BlockChest) world.getType(position).getBlock()).g == Type.TRAP ?
                Block.getByName("trapped_chest") : Block.getByName("chest"));

        // If block on top
        if (topBlocking(world, position)) {
            return true;
        }

        // If block next to chest is chest and has a block on top
        for (EnumDirection direction : EnumDirectionList.HORIZONTAL) {
            BlockPosition sidePosition = position.shift(direction);
            Block var8 = world.getType(sidePosition).getBlock();
            if (var8 == chest) {
                if (this.topBlocking(world, sidePosition)) {
                    return true;
                }
            }
        }

        return false;
    }
    private boolean topBlocking(World world, BlockPosition position) {
        return this.blockOnTop(world, position) || this.ocelotOnTop(world, position);
    }

    private boolean blockOnTop(World world, BlockPosition position) {
        Block block = world.getType(position.up()).getBlock();
        return block.isOccluding(block.getBlockData());
    }

    private boolean ocelotOnTop(World world, BlockPosition position) {
        Iterator var3 = world.a(EntityOcelot.class,
                new AxisAlignedBB((double) position.getX(), (double) (position.getY() + 1),
                        (double) position.getZ(), (double) (position.getX() + 1),
                        (double) (position.getY() + 2), (double) (position.getZ() + 1))).iterator();

        EntityOcelot var5;
        do {
            if (!var3.hasNext()) {
                return false;
            }

            Entity var4 = (Entity) var3.next();
            var5 = (EntityOcelot) var4;
        } while (!var5.isSitting());

        return true;
    }

    public boolean activateChest(Player p, boolean anyChest, boolean silentChest, int x, int y, int z) {
        BlockPosition position = new BlockPosition(x, y, z);
        EntityPlayer player = ((CraftPlayer) p).getHandle();
        World world = player.world;
        if (world.isClientSide) {
            return true;
        }

        BlockChest chest = (BlockChest) (((BlockChest) world.getType(position).getBlock()).g == Type.TRAP ?
                Block.getByName("trapped_chest") : Block.getByName("chest"));

        TileEntity tileEntity = world.getTileEntity(position);
        if (!(tileEntity instanceof TileEntityChest)) {
            return true;
        }

        ITileInventory tileInventory = (ITileInventory) tileEntity;
        if (!anyChest && this.topBlocking(world, position)) {
            return true;
        }

        for (EnumDirection direction : EnumDirectionList.HORIZONTAL) {
            BlockPosition side = position.shift(direction);
            Block block = world.getType(side).getBlock();
            if (block == chest) {
                if (!anyChest && this.topBlocking(world, side)) {
                    return true;
                }

                TileEntity sideTileEntity = world.getTileEntity(side);
                if (sideTileEntity instanceof TileEntityChest) {
                    if (direction != EnumDirection.WEST && direction != EnumDirection.NORTH) {
                        tileInventory = new InventoryLargeChest("container.chestDouble", tileInventory, (TileEntityChest) sideTileEntity);
                    } else {
                        tileInventory = new InventoryLargeChest("container.chestDouble", (TileEntityChest) sideTileEntity, tileInventory);
                    }
                }
            }
        }

        boolean returnValue = true;
        if (silentChest) {
            tileInventory = new SilentInventory(tileInventory);
            if (OpenInv.notifySilentChest()) {
                OpenInv.sendMessage(p, "You are opening a chest silently.");
            }
            returnValue = false;
        }

        player.openContainer(tileInventory);

        if (anyChest && OpenInv.notifyAnyChest()) {
            OpenInv.sendMessage(p, "You are opening a blocked chest.");
        }

        return returnValue;
    }
}
