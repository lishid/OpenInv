/*
 * Copyright (C) 2011-2019 lishid. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.openinv.internal.v1_13_R2;

import com.lishid.openinv.internal.IAnySilentContainer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.server.v1_13_R2.AxisAlignedBB;
import net.minecraft.server.v1_13_R2.Block;
import net.minecraft.server.v1_13_R2.BlockChest;
import net.minecraft.server.v1_13_R2.BlockChestTrapped;
import net.minecraft.server.v1_13_R2.BlockEnderChest;
import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.BlockPropertyChestType;
import net.minecraft.server.v1_13_R2.BlockShulkerBox;
import net.minecraft.server.v1_13_R2.ChatMessage;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityOcelot;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EnumDirection;
import net.minecraft.server.v1_13_R2.EnumGamemode;
import net.minecraft.server.v1_13_R2.IBlockData;
import net.minecraft.server.v1_13_R2.ITileInventory;
import net.minecraft.server.v1_13_R2.InventoryEnderChest;
import net.minecraft.server.v1_13_R2.InventoryLargeChest;
import net.minecraft.server.v1_13_R2.PlayerInteractManager;
import net.minecraft.server.v1_13_R2.TileEntity;
import net.minecraft.server.v1_13_R2.TileEntityChest;
import net.minecraft.server.v1_13_R2.TileEntityEnderChest;
import net.minecraft.server.v1_13_R2.TileEntityShulkerBox;
import net.minecraft.server.v1_13_R2.VoxelShape;
import net.minecraft.server.v1_13_R2.VoxelShapes;
import net.minecraft.server.v1_13_R2.World;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

public class AnySilentContainer implements IAnySilentContainer {

    private Field playerInteractManagerGamemode;

    public AnySilentContainer() {
        try {
            this.playerInteractManagerGamemode = PlayerInteractManager.class.getDeclaredField("gamemode");
            this.playerInteractManagerGamemode.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException e) {
            System.err.println("[OpenInv] Unable to directly write player gamemode! SilentChest will fail.");
            e.printStackTrace();
        }
    }

    @Override
    public boolean isAnySilentContainer(@NotNull final org.bukkit.block.Block bukkitBlock) {
        if (bukkitBlock.getType() == Material.ENDER_CHEST) {
            return true;
        }
        BlockState state = bukkitBlock.getState();
        return state instanceof org.bukkit.block.Chest
                || state instanceof org.bukkit.block.ShulkerBox;
    }

    @Override
    public boolean isAnyContainerNeeded(@NotNull final Player bukkitPlayer, @NotNull final org.bukkit.block.Block bukkitBlock) {

        World world = PlayerDataManager.getHandle(bukkitPlayer).world;
        BlockPosition blockPosition = new BlockPosition(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        IBlockData blockData = world.getType(blockPosition);
        Block block = blockData.getBlock();

        if (block instanceof BlockShulkerBox) {
            return this.isBlockedShulkerBox(world, blockPosition, blockData);
        }

        if (block instanceof BlockEnderChest) {
            // Ender chests are not blocked by ocelots.
            return world.getType(blockPosition.up()).isOccluding();
        }

        // Check if chest is blocked or has an ocelot on top
        if (this.isBlockedChest(world, blockPosition)) {
            return true;
        }

        // Check for matching adjacent chests that are blocked or have an ocelot on top
        BlockPropertyChestType chestType = blockData.get(BlockChest.b);

        if (chestType == BlockPropertyChestType.SINGLE) {
            return false;
        }

        BlockPosition adjacentBlockPosition = blockPosition.shift(BlockChest.k(blockData));
        IBlockData adjacentBlockData = world.getType(adjacentBlockPosition);

        if (adjacentBlockData.getBlock() == block) {

            BlockPropertyChestType adjacentChestType = adjacentBlockData.get(BlockChest.b);

            if (adjacentChestType != BlockPropertyChestType.SINGLE && chestType != adjacentChestType
                    && adjacentBlockData.get(BlockChest.FACING) == blockData.get(BlockChest.FACING)) {

                return this.isBlockedChest(world, adjacentBlockPosition);
            }
        }

        return false;
    }

    private boolean isBlockedShulkerBox(final World world, final BlockPosition blockPosition,
                                        final IBlockData blockData) {
        // For reference, look at net.minecraft.server.BlockShulkerBox
        TileEntity tile = world.getTileEntity(blockPosition);

        if (!(tile instanceof TileEntityShulkerBox)) {
            return false;
        }

        EnumDirection enumDirection = blockData.get(BlockShulkerBox.a);
        if (((TileEntityShulkerBox) tile).r() == TileEntityShulkerBox.AnimationPhase.CLOSED) {
            AxisAlignedBB axisAlignedBB;
            try {
                Method method = VoxelShape.class.getMethod("a");
                axisAlignedBB = (AxisAlignedBB) method.invoke(VoxelShapes.b());
            } catch (NoSuchMethodException e) {
                axisAlignedBB = VoxelShapes.b().getBoundingBox();
            } catch (InvocationTargetException | IllegalAccessException e) {
                return false;
            }
            axisAlignedBB = axisAlignedBB
                    .b(0.5F * enumDirection.getAdjacentX(), 0.5F * enumDirection.getAdjacentY(), 0.5F * enumDirection.getAdjacentZ())
                    .a(enumDirection.getAdjacentX(), enumDirection.getAdjacentY(), enumDirection.getAdjacentZ());
            return !world.getCubes(null, axisAlignedBB.a(blockPosition.shift(enumDirection)));
        }

        return false;
    }

    private boolean isBlockedChest(final World world, final BlockPosition blockPosition) {
        // For reference, loot at net.minecraft.server.BlockChest
        return world.getType(blockPosition.up()).isOccluding() || this.hasOcelotOnTop(world, blockPosition);
    }

    private boolean hasOcelotOnTop(final World world, final BlockPosition blockPosition) {
        for (Entity entity : world.a(EntityOcelot.class,
                new AxisAlignedBB(blockPosition.getX(), blockPosition.getY() + 1,
                        blockPosition.getZ(), blockPosition.getX() + 1, blockPosition.getY() + 2,
                        blockPosition.getZ() + 1))) {
            EntityOcelot entityOcelot = (EntityOcelot) entity;
            if (entityOcelot.isSitting()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean activateContainer(@NotNull final Player bukkitPlayer, final boolean silent,
                                     @NotNull final org.bukkit.block.Block bukkitBlock) {

        // Silent ender chest is API-only
        if (silent && bukkitBlock.getType() == Material.ENDER_CHEST) {
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
        IBlockData blockData = world.getType(blockPosition);
        Block block = blockData.getBlock();

        if (block instanceof BlockChest) {

            BlockPropertyChestType chestType = blockData.get(BlockChest.b);

            if (chestType != BlockPropertyChestType.SINGLE) {

                BlockPosition adjacentBlockPosition = blockPosition.shift(BlockChest.k(blockData));
                IBlockData adjacentBlockData = world.getType(adjacentBlockPosition);

                if (adjacentBlockData.getBlock() == block) {

                    BlockPropertyChestType adjacentChestType = adjacentBlockData.get(BlockChest.b);

                    if (adjacentChestType != BlockPropertyChestType.SINGLE && chestType != adjacentChestType
                            && adjacentBlockData.get(BlockChest.FACING) == blockData.get(BlockChest.FACING)) {

                        TileEntity adjacentTile = world.getTileEntity(adjacentBlockPosition);

                        if (adjacentTile instanceof TileEntityChest) {
                            ITileInventory rightChest = chestType == BlockPropertyChestType.RIGHT ? tileInventory : (ITileInventory) adjacentTile;
                            ITileInventory leftChest = chestType == BlockPropertyChestType.RIGHT ? (ITileInventory) adjacentTile : tileInventory;
                            tileInventory = new InventoryLargeChest(new ChatMessage("container.chestDouble"), rightChest, leftChest);
                        }
                    }
                }
            }

            if (block instanceof BlockChestTrapped) {
                bukkitPlayer.incrementStatistic(Statistic.TRAPPED_CHEST_TRIGGERED);
            } else {
                bukkitPlayer.incrementStatistic(Statistic.CHEST_OPENED);
            }
        }

        if (block instanceof BlockShulkerBox) {
            bukkitPlayer.incrementStatistic(Statistic.SHULKER_BOX_OPENED);
        }

        // AnyChest only - SilentChest not active, container unsupported, or unnecessary.
        if (!silent || player.playerInteractManager.getGameMode() == EnumGamemode.SPECTATOR) {
            player.openContainer(tileInventory);
            return true;
        }

        // SilentChest requires access to setting players' gamemode directly.
        if (this.playerInteractManagerGamemode == null) {
            return false;
        }

        EnumGamemode gamemode = player.playerInteractManager.getGameMode();
        this.forceGameMode(player, EnumGamemode.SPECTATOR);
        player.openContainer(tileInventory);
        this.forceGameMode(player, gamemode);
        return true;
    }

    @Override
    public void deactivateContainer(@NotNull final Player bukkitPlayer) {
        if (this.playerInteractManagerGamemode == null) {
            return;
        }

        InventoryView view = bukkitPlayer.getOpenInventory();
        switch (view.getType()) {
        case CHEST:
        case ENDER_CHEST:
        case SHULKER_BOX:
            break;
        default:
            return;
        }

        EntityPlayer player = PlayerDataManager.getHandle(bukkitPlayer);

        EnumGamemode gamemode = player.playerInteractManager.getGameMode();
        this.forceGameMode(player, EnumGamemode.SPECTATOR);
        player.activeContainer.b(player);
        player.activeContainer = player.defaultContainer;
        this.forceGameMode(player, gamemode);
    }

    private void forceGameMode(final EntityPlayer player, final EnumGamemode gameMode) {
        if (this.playerInteractManagerGamemode == null) {
            // No need to warn repeatedly, error on startup and lack of function should be enough.
            return;
        }
        try {
            if (!this.playerInteractManagerGamemode.isAccessible()) {
                // Just in case, ensure accessible.
                this.playerInteractManagerGamemode.setAccessible(true);
            }
            this.playerInteractManagerGamemode.set(player.playerInteractManager, gameMode);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
