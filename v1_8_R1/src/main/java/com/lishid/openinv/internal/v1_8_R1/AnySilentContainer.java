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

import com.lishid.openinv.internal.IAnySilentContainer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

//Volatile
import net.minecraft.server.v1_8_R1.AxisAlignedBB;
import net.minecraft.server.v1_8_R1.Block;
import net.minecraft.server.v1_8_R1.BlockChest;
import net.minecraft.server.v1_8_R1.BlockEnderChest;
import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.EntityOcelot;
import net.minecraft.server.v1_8_R1.EntityPlayer;
import net.minecraft.server.v1_8_R1.EnumDirection;
import net.minecraft.server.v1_8_R1.IInventory;
import net.minecraft.server.v1_8_R1.ITileInventory;
import net.minecraft.server.v1_8_R1.InventoryEnderChest;
import net.minecraft.server.v1_8_R1.InventoryLargeChest;
import net.minecraft.server.v1_8_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_8_R1.TileEntity;
import net.minecraft.server.v1_8_R1.TileEntityChest;
import net.minecraft.server.v1_8_R1.TileEntityEnderChest;
import net.minecraft.server.v1_8_R1.World;

import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;

public class AnySilentContainer implements IAnySilentContainer {

    @Override
    public boolean isAnySilentContainer(org.bukkit.block.Block block) {
        return block.getType() == Material.ENDER_CHEST || block.getState() instanceof org.bukkit.block.Chest;
    }

    @Override
    public boolean isAnyContainerNeeded(Player p, org.bukkit.block.Block b) {
        EntityPlayer player = ((CraftPlayer) p).getHandle();
        World world = player.world;
        BlockPosition blockPosition = new BlockPosition(b.getX(), b.getY(), b.getZ());
        Block block = world.getType(blockPosition).getBlock();

        if (block instanceof BlockEnderChest) {
            // Ender chests are not blocked by ocelots.
            return world.getType(blockPosition.up()).getBlock().c();
        }

        // Check if chest is blocked or has an ocelot on top
        if (isBlockedChest(world, blockPosition)) {
            return true;
        }

        // Check for matching adjacent chests that are blocked or have an ocelot on top
        for (EnumDirection localEnumDirection : EnumDirection.values()) {
            if (!localEnumDirection.k().c()) {
                // Not horizontal
                continue;
            }

            BlockPosition localBlockPosition = blockPosition.shift(localEnumDirection);
            Block localBlock = world.getType(localBlockPosition).getBlock();

            if (localBlock != block) {
                continue;
            }

            TileEntity localTileEntity = world.getTileEntity(localBlockPosition);
            if (!(localTileEntity instanceof TileEntityChest)) {
                continue;
            }

            if (isBlockedChest(world, localBlockPosition)) {
                return true;
            }
        }

        return false;
    }

    private boolean isBlockedChest(World world, BlockPosition blockPosition) {
        // For reference, loot at net.minecraft.server.BlockChest
        return world.getType(blockPosition.up()).getBlock().c() || hasOcelotOnTop(world, blockPosition);
    }

    private boolean hasOcelotOnTop(World world, BlockPosition blockPosition) {
        for (Object localEntity : world.a(EntityOcelot.class,
                new AxisAlignedBB(blockPosition.getX(), blockPosition.getY() + 1,
                        blockPosition.getZ(), blockPosition.getX() + 1, blockPosition.getY() + 2,
                        blockPosition.getZ() + 1))) {
            EntityOcelot localEntityOcelot = (EntityOcelot) localEntity;
            if (localEntityOcelot.isSitting()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean activateContainer(Player p, boolean silentchest, org.bukkit.block.Block b) {

        EntityPlayer player = ((CraftPlayer) p).getHandle();

        // Silent ender chest is API-only
        if (silentchest && b.getType() == Material.ENDER_CHEST) {
            p.openInventory(p.getEnderChest());
            return true;
        }

        World world = player.world;
        BlockPosition blockPosition = new BlockPosition(b.getX(), b.getY(), b.getZ());
        Object tile = world.getTileEntity(blockPosition);

        if (tile == null) {
            return false;
        }

        if (tile instanceof TileEntityEnderChest) {
            // Anychest ender chest. See net.minecraft.server.BlockEnderChest
            InventoryEnderChest enderChest = player.getEnderChest();
            enderChest.a((TileEntityEnderChest) tile);
            player.openContainer(enderChest);
            return true;
        }

        if (!(tile instanceof IInventory)) {
            return false;
        }

        Block block = world.getType(blockPosition).getBlock();

        if (block instanceof BlockChest) {
            for (EnumDirection localEnumDirection : EnumDirection.values()) {
                if (!localEnumDirection.k().c()) {
                    // Not horizontal
                    continue;
                }

                BlockPosition localBlockPosition = blockPosition.shift(localEnumDirection);
                Block localBlock = world.getType(localBlockPosition).getBlock();

                if (localBlock != block) {
                    continue;
                }

                TileEntity localTileEntity = world.getTileEntity(localBlockPosition);
                if (!(localTileEntity instanceof TileEntityChest)) {
                    continue;
                }

                if ((localEnumDirection == EnumDirection.WEST) || (localEnumDirection == EnumDirection.NORTH)) {
                    tile = new InventoryLargeChest("container.chestDouble",
                            (TileEntityChest) localTileEntity, (ITileInventory) tile);
                } else {
                    tile = new InventoryLargeChest("container.chestDouble",
                            (ITileInventory) tile, (TileEntityChest) localTileEntity);
                }
                break;
            }

            if (silentchest) {
                tile = new SilentContainerChest(player.inventory, ((IInventory) tile), player);
            }
        }

        boolean returnValue = false;
        if (!silentchest) {
            player.openContainer((IInventory) tile);
            returnValue = true;
        } else {
            try {
                SilentContainerChest silentContainerChest = new SilentContainerChest(player.inventory, ((IInventory) tile), player);
                int windowId = player.nextContainerCounter();
                player.playerConnection.sendPacket(new PacketPlayOutOpenWindow(windowId, "minecraft:chest", ((IInventory) tile).getScoreboardDisplayName(), ((IInventory) tile).getSize()));
                player.activeContainer = silentContainerChest;
                player.activeContainer.windowId = windowId;
                player.activeContainer.addSlotListener(player);
                returnValue = true;
            } catch (Exception e) {
                e.printStackTrace();
                p.sendMessage(ChatColor.RED + "Error while sending silent chest.");
            }
        }

        return returnValue;
    }

}
