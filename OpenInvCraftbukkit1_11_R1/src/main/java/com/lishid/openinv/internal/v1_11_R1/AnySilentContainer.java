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

package com.lishid.openinv.internal.v1_11_R1;

import com.lishid.openinv.internal.IAnySilentContainer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

// Volatile
import net.minecraft.server.v1_11_R1.AxisAlignedBB;
import net.minecraft.server.v1_11_R1.Block;
import net.minecraft.server.v1_11_R1.BlockChest;
import net.minecraft.server.v1_11_R1.BlockChest.Type;
import net.minecraft.server.v1_11_R1.BlockEnderChest;
import net.minecraft.server.v1_11_R1.BlockPosition;
import net.minecraft.server.v1_11_R1.BlockShulkerBox;
import net.minecraft.server.v1_11_R1.Container;
import net.minecraft.server.v1_11_R1.Entity;
import net.minecraft.server.v1_11_R1.EntityOcelot;
import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.EnumDirection;
import net.minecraft.server.v1_11_R1.IBlockData;
import net.minecraft.server.v1_11_R1.IInventory;
import net.minecraft.server.v1_11_R1.ITileInventory;
import net.minecraft.server.v1_11_R1.InventoryLargeChest;
import net.minecraft.server.v1_11_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_11_R1.StatisticList;
import net.minecraft.server.v1_11_R1.TileEntity;
import net.minecraft.server.v1_11_R1.TileEntityChest;
import net.minecraft.server.v1_11_R1.TileEntityShulkerBox;
import net.minecraft.server.v1_11_R1.World;

import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;

public class AnySilentContainer implements IAnySilentContainer {

    @Override
    public boolean isAnySilentContainer(org.bukkit.block.Block block) {
        if (block.getType() == Material.ENDER_CHEST) {
            return true;
        }
        BlockState state = block.getState();
        return state instanceof org.bukkit.block.Chest || state instanceof org.bukkit.block.ShulkerBox;
    }

    @Override
    public boolean activateContainer(Player p, boolean silentchest, int x, int y, int z) {

        // TODO backport to all modules? TODO change new API to activateContainer(Player, Block)? Use BlockState instead?
        if (silentchest && p.getWorld().getBlockAt(x, y, z).getType() == Material.ENDER_CHEST) {
            p.openInventory(p.getEnderChest());
            return true;
        }

        EntityPlayer player = ((CraftPlayer) p).getHandle();
        final World world = player.world;
        final BlockPosition blockPosition = new BlockPosition(x, y, z);
        Object tile = world.getTileEntity(blockPosition);

        if (tile == null) {
            return false;
        }

        Block block = world.getType(blockPosition).getBlock();
        Container container = null;

        if (block instanceof BlockChest) {
            BlockChest blockChest = (BlockChest) block;

            for (EnumDirection enumDirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
                BlockPosition localBlockPosition = blockPosition.shift(enumDirection);
                Block localBlock = world.getType(localBlockPosition).getBlock();

                if (localBlock != block) {
                    continue;
                }

                TileEntity localTileEntity = world.getTileEntity(localBlockPosition);
                if (!(localTileEntity instanceof TileEntityChest)) {
                    continue;
                }

                if ((enumDirection == EnumDirection.WEST) || (enumDirection == EnumDirection.NORTH)) {
                    tile = new InventoryLargeChest("container.chestDouble",
                            (TileEntityChest) localTileEntity, (ITileInventory) tile);
                } else {
                    tile = new InventoryLargeChest("container.chestDouble",
                            (ITileInventory) tile, (TileEntityChest) localTileEntity);
                }
                break;
            }

            if (blockChest.g == Type.BASIC)
                player.b(StatisticList.ac);
            else if (blockChest.g == Type.TRAP) {
                player.b(StatisticList.W);
            }

            if (silentchest) {
                container = new SilentContainerChest(player.inventory, ((IInventory) tile), player);
            }
        }

        if (block instanceof BlockShulkerBox) {
            player.b(StatisticList.ae);

            if (silentchest && tile instanceof TileEntityShulkerBox) {
                // Set value to current + 1. Ensures consistency later when resetting.
                SilentContainerShulkerBox.setOpenValue((TileEntityShulkerBox) tile,
                        SilentContainerShulkerBox.getOpenValue((TileEntityShulkerBox) tile) + 1);

                container = new SilentContainerShulkerBox(player.inventory, (IInventory) tile, player);
            }
        }

        if (!(tile instanceof IInventory)) {
            // TODO anyenderchest
            p.sendMessage(ChatColor.RED + "Unhandled non-IInventory for block!");
            return false;
        }

        boolean returnValue = false;
        final IInventory iInventory = (IInventory) tile;

        if (!silentchest || container == null) {
            player.openContainer(iInventory);
            returnValue = true;
        } else {
            try {
                int windowId = player.nextContainerCounter();
                player.playerConnection.sendPacket(new PacketPlayOutOpenWindow(windowId, iInventory.getName(), iInventory.getScoreboardDisplayName(), iInventory.getSize()));
                player.activeContainer = container;
                player.activeContainer.windowId = windowId;
                player.activeContainer.addSlotListener(player);
                returnValue = true;
                if (tile instanceof TileEntityShulkerBox) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // TODO hacky
                            Object tile = world.getTileEntity(blockPosition);
                            if (!(tile instanceof TileEntityShulkerBox)) {
                                return;
                            }
                            TileEntityShulkerBox box = (TileEntityShulkerBox) tile;
                            // Reset back - we added 1, and calling TileEntityShulkerBox#startOpen adds 1 more.
                            SilentContainerShulkerBox.setOpenValue(box,
                                    SilentContainerShulkerBox.getOpenValue((TileEntityShulkerBox) tile) - 2);
                        }
                    }.runTaskLater(Bukkit.getPluginManager().getPlugin("OpenInv"), 2);
                }
            } catch (Exception e) {
                e.printStackTrace();
                p.sendMessage(ChatColor.RED + "Error while sending silent chest.");
            }
        }

        return returnValue;
    }

    @Override
    public boolean isAnyContainerNeeded(Player p, int x, int y, int z) {
        EntityPlayer player = ((CraftPlayer) p).getHandle();
        World world = player.world;
        BlockPosition blockPosition = new BlockPosition(x, y, z);
        Block block = world.getType(blockPosition).getBlock();

        if (block instanceof BlockShulkerBox) {
            return isBlockedShulkerBox(world, blockPosition, block);
        }

        if (block instanceof BlockEnderChest) {
            // Ender chests are not blocked by ocelots.
            return world.getType(new BlockPosition(x, y + 1, z)).m();
        }

        // Check if chest is blocked or has an ocelot on top
        if (isBlockedChest(world, blockPosition)) {
            return true;
        }

        // Check for matching adjacent chests that are blocked or have an ocelot on top
        for (EnumDirection localEnumDirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
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

    private boolean isBlockedShulkerBox(World world, BlockPosition blockPosition, Block block) {
        // For reference, look at net.minecraft.server.BlockShulkerBox
        TileEntity tile = world.getTileEntity(blockPosition);

        if (!(tile instanceof TileEntityShulkerBox)) {
            return false;
        }

        IBlockData iBlockData = block.getBlockData();

        EnumDirection enumDirection = iBlockData.get(BlockShulkerBox.a);
        if (((TileEntityShulkerBox) tile).p() == TileEntityShulkerBox.AnimationPhase.CLOSED) {
            AxisAlignedBB axisAlignedBB = BlockShulkerBox.j.b(0.5F * enumDirection.getAdjacentX(),
                    0.5F * enumDirection.getAdjacentY(), 0.5F * enumDirection.getAdjacentZ())
                    .a(enumDirection.getAdjacentX(), enumDirection.getAdjacentY(),
                            enumDirection.getAdjacentZ());

            return world.b(axisAlignedBB.a(blockPosition.shift(enumDirection)));
        }

        return false;
    }

    private boolean isBlockedChest(World world, BlockPosition blockPosition) {
        // For reference, loot at net.minecraft.server.BlockChest
        return world.getType(blockPosition.up()).m() || hasOcelotOnTop(world, blockPosition);
    }

    private boolean hasOcelotOnTop(World world, BlockPosition blockPosition) {
        for (Entity localEntity : world.a(EntityOcelot.class,
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

    /**
     * @deprecated Use {@link #activateContainer(Player, boolean, boolean, int, int, int)}.
     */
    @Deprecated
    @Override
    public boolean activateChest(Player player, boolean anychest, boolean silentchest, int x, int y, int z) {
        return !activateContainer(player, silentchest, x, y, z);
    }

    /**
     * @deprecated Use {@link #isAnyContainerNeeded(Player, int, int, int)}.
     */
    @Deprecated
    @Override
    public boolean isAnyChestNeeded(Player player, int x, int y, int z) {
        return isAnyContainerNeeded(player, x, y, z);
    }

}
