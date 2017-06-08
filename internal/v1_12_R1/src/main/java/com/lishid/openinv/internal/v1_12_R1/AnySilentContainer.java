/*
 * Copyright (C) 2011-2014 lishid. All rights reserved. This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, version 3. This program is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should
 * have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.lishid.openinv.internal.v1_12_R1;

import com.lishid.openinv.internal.IAnySilentContainer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.server.v1_12_R1.AxisAlignedBB;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockChest;
import net.minecraft.server.v1_12_R1.BlockEnderChest;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.BlockShulkerBox;
import net.minecraft.server.v1_12_R1.Container;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityOcelot;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EnumDirection;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.ITileInventory;
import net.minecraft.server.v1_12_R1.InventoryEnderChest;
import net.minecraft.server.v1_12_R1.InventoryLargeChest;
import net.minecraft.server.v1_12_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_12_R1.TileEntity;
import net.minecraft.server.v1_12_R1.TileEntityChest;
import net.minecraft.server.v1_12_R1.TileEntityEnderChest;
import net.minecraft.server.v1_12_R1.TileEntityShulkerBox;
import net.minecraft.server.v1_12_R1.World;

import org.bukkit.craftbukkit.v1_12_R1.event.CraftEventFactory;

public class AnySilentContainer implements IAnySilentContainer {

    @Override
    public boolean activateContainer(final Player bukkitPlayer, final boolean silentchest,
            final org.bukkit.block.Block bukkitBlock) {

        // Silent ender chest is API-only
        if (silentchest && bukkitBlock.getType() == Material.ENDER_CHEST) {
            bukkitPlayer.openInventory(bukkitPlayer.getEnderChest());
            bukkitPlayer.incrementStatistic(Statistic.ENDERCHEST_OPENED);
            return true;
        }

        EntityPlayer player = PlayerDataManager.getHandle(bukkitPlayer);

        final World world = player.world;
        final BlockPosition blockPosition = new BlockPosition(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        final Object tile = world.getTileEntity(blockPosition);

        if (tile == null) {
            return false;
        }

        if (tile instanceof TileEntityEnderChest) {
            // Anychest ender chest. See net.minecraft.server.BlockEnderChest
            InventoryEnderChest enderChest = player.getEnderChest();
            enderChest.a((TileEntityEnderChest) tile);
            player.openContainer(enderChest);
            bukkitPlayer.incrementStatistic(Statistic.ENDERCHEST_OPENED);
            return true;
        }

        if (!(tile instanceof ITileInventory)) {
            return false;
        }

        ITileInventory tileInventory = (ITileInventory) tile;
        Block block = world.getType(blockPosition).getBlock();
        Container container = null;

        if (block instanceof BlockChest) {
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

                if (localEnumDirection == EnumDirection.WEST
                        || localEnumDirection == EnumDirection.NORTH) {
                    tileInventory = new InventoryLargeChest("container.chestDouble",
                            (TileEntityChest) localTileEntity, tileInventory);
                } else {
                    tileInventory = new InventoryLargeChest("container.chestDouble", tileInventory,
                            (TileEntityChest) localTileEntity);
                }
                break;
            }

            BlockChest blockChest = (BlockChest) block;
            if (blockChest.g == BlockChest.Type.BASIC) {
                bukkitPlayer.incrementStatistic(Statistic.CHEST_OPENED);
            } else if (blockChest.g == BlockChest.Type.TRAP) {
                bukkitPlayer.incrementStatistic(Statistic.TRAPPED_CHEST_TRIGGERED);
            }

            if (silentchest) {
                container = new SilentContainerChest(player.inventory, tileInventory, player);
            }
        }

        if (block instanceof BlockShulkerBox) {
            bukkitPlayer.incrementStatistic(Statistic.SHULKER_BOX_OPENED);

            if (silentchest && tileInventory instanceof TileEntityShulkerBox) {
                // Set value to current + 1. Ensures consistency later when resetting.
                SilentContainerShulkerBox.setOpenValue((TileEntityShulkerBox) tileInventory,
                        SilentContainerShulkerBox.getOpenValue((TileEntityShulkerBox) tileInventory)
                                + 1);

                container = new SilentContainerShulkerBox(player.inventory, tileInventory, player);
            }
        }

        // AnyChest only - SilentChest not active or container unsupported
        if (!silentchest || container == null) {
            player.openContainer(tileInventory);
            return true;
        }

        // SilentChest
        try {
            // Call InventoryOpenEvent
            container = CraftEventFactory.callInventoryOpenEvent(player, container, false);
            if (container == null) {
                return false;
            }

            // Open window
            int windowId = player.nextContainerCounter();
            player.playerConnection.sendPacket(
                    new PacketPlayOutOpenWindow(windowId, tileInventory.getContainerName(),
                            tileInventory.getScoreboardDisplayName(), tileInventory.getSize()));
            player.activeContainer = container;
            player.activeContainer.windowId = windowId;
            player.activeContainer.addSlotListener(player);

            // Special handling for shulker boxes - reset value for viewers to what it was initially.
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
                                SilentContainerShulkerBox.getOpenValue((TileEntityShulkerBox) tile)
                                        - 2);
                    }
                }.runTaskLater(Bukkit.getPluginManager().getPlugin("OpenInv"), 2);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            bukkitPlayer.sendMessage(ChatColor.RED + "Error while sending silent container.");
            return false;
        }
    }

    private boolean hasOcelotOnTop(final World world, final BlockPosition blockPosition) {
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

    @Override
    public boolean isAnyContainerNeeded(final Player p, final org.bukkit.block.Block b) {
        EntityPlayer player = PlayerDataManager.getHandle(p);
        World world = player.world;
        BlockPosition blockPosition = new BlockPosition(b.getX(), b.getY(), b.getZ());
        IBlockData blockData = world.getType(blockPosition);
        Block block = blockData.getBlock();

        if (block instanceof BlockShulkerBox) {
            return this.isBlockedShulkerBox(world, blockPosition, blockData);
        }

        if (block instanceof BlockEnderChest) {
            // Ender chests are not blocked by ocelots.
            return world.getType(blockPosition.up()).m();
        }

        // Check if chest is blocked or has an ocelot on top
        if (this.isBlockedChest(world, blockPosition)) {
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

            if (this.isBlockedChest(world, localBlockPosition)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isAnySilentContainer(final org.bukkit.block.Block block) {
        if (block.getType() == Material.ENDER_CHEST) {
            return true;
        }
        BlockState state = block.getState();
        return state instanceof org.bukkit.block.Chest
                || state instanceof org.bukkit.block.ShulkerBox;
    }

    private boolean isBlockedChest(final World world, final BlockPosition blockPosition) {
        // For reference, loot at net.minecraft.server.BlockChest
        return world.getType(blockPosition.up()).l() || this.hasOcelotOnTop(world, blockPosition);
    }

    private boolean isBlockedShulkerBox(final World world, final BlockPosition blockPosition,
            final IBlockData blockData) {
        // For reference, look at net.minecraft.server.BlockShulkerBox
        TileEntity tile = world.getTileEntity(blockPosition);

        if (!(tile instanceof TileEntityShulkerBox)) {
            return false;
        }

        EnumDirection enumDirection = blockData.get(BlockShulkerBox.a);
        if (((TileEntityShulkerBox) tile).p() == TileEntityShulkerBox.AnimationPhase.CLOSED) {
            AxisAlignedBB axisAlignedBB = Block.j.b(0.5F * enumDirection.getAdjacentX(),
                    0.5F * enumDirection.getAdjacentY(), 0.5F * enumDirection.getAdjacentZ())
                    .a(enumDirection.getAdjacentX(), enumDirection.getAdjacentY(),
                            enumDirection.getAdjacentZ());

            return world.a(axisAlignedBB.a(blockPosition.shift(enumDirection)));
        }

        return false;
    }

}
