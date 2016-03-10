/*
 * Copyright (C) 2011-2016 lishid.  All rights reserved.
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

import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.lishid.openinv.OpenInv;

import net.minecraft.server.v1_9_R1.AxisAlignedBB;
import net.minecraft.server.v1_9_R1.Block;
import net.minecraft.server.v1_9_R1.BlockChest;
import net.minecraft.server.v1_9_R1.BlockChest.Type;
import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.EntityOcelot;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.EnumDirection;
import net.minecraft.server.v1_9_R1.ITileInventory;
import net.minecraft.server.v1_9_R1.InventoryLargeChest;
import net.minecraft.server.v1_9_R1.TileEntity;
import net.minecraft.server.v1_9_R1.TileEntityChest;
import net.minecraft.server.v1_9_R1.World;

public class AnySilentChest {

    private final OpenInv plugin;

    public AnySilentChest(OpenInv plugin) {
        this.plugin = plugin;
    }

    public boolean isAnyChestNeeded(Player p, int x, int y, int z) {
        // FOR REFERENCE, LOOK AT net.minecraft.server.BlockChest
        BlockPosition position = new BlockPosition(x, y, z);
        EntityPlayer player = ((CraftPlayer) p).getHandle();
        World world = player.world;
        BlockChest chest = (BlockChest) (((BlockChest) world.getType(position).getBlock()).g == Type.TRAP ?
                Block.getByName("trapped_chest") : Block.getByName("chest"));

        // If a block is on top
        if (topBlocking(world, position)) {
            return true;
        }

        // If the block next to the chest is chest and has a block on top
        for (EnumDirection direction : EnumDirectionList.HORIZONTAL) {
            BlockPosition sidePosition = position.shift(direction);
            Block block = world.getType(sidePosition).getBlock();

            if (block == chest) {
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
        Iterator iterator = world.a(EntityOcelot.class,
                new AxisAlignedBB((double) position.getX(), (double) (position.getY() + 1),
                        (double) position.getZ(), (double) (position.getX() + 1),
                        (double) (position.getY() + 2), (double) (position.getZ() + 1))).iterator();

        EntityOcelot entityOcelot;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            Entity entity = (Entity) iterator.next();

            entityOcelot = (EntityOcelot) entity;
        } while (!entityOcelot.isSitting());

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

            if (plugin.getConfiguration().notifySilentChest()) {
                OpenInv.sendMessage(p, "You are opening a chest silently.");
            }

            returnValue = false;
        }

        player.openContainer(tileInventory);

        if (anyChest && plugin.getConfiguration().notifyAnyChest()) {
            OpenInv.sendMessage(p, "You are opening a blocked chest.");
        }

        return returnValue;
    }
}
