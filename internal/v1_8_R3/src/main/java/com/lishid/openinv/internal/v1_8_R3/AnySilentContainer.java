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

package com.lishid.openinv.internal.v1_8_R3;

import com.lishid.openinv.internal.IAnySilentContainer;
import java.lang.reflect.Field;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockChest;
import net.minecraft.server.v1_8_R3.BlockEnderChest;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityOcelot;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EnumDirection;
import net.minecraft.server.v1_8_R3.ITileInventory;
import net.minecraft.server.v1_8_R3.InventoryEnderChest;
import net.minecraft.server.v1_8_R3.InventoryLargeChest;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.StatisticList;
import net.minecraft.server.v1_8_R3.TileEntity;
import net.minecraft.server.v1_8_R3.TileEntityChest;
import net.minecraft.server.v1_8_R3.TileEntityEnderChest;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldSettings.EnumGamemode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

public class AnySilentContainer implements IAnySilentContainer {

    private Field playerInteractManagerGamemode;

    public AnySilentContainer() {
        try {
            this.playerInteractManagerGamemode = PlayerInteractManager.class.getDeclaredField("gamemode");
            this.playerInteractManagerGamemode.setAccessible(true);
        } catch (Exception e) {
            System.err.println("[OpenInv] Unable to directly write player gamemode! SilentChest will fail.");
            e.printStackTrace();
        }
    }

    @Override
    public boolean isAnySilentContainer(@NotNull org.bukkit.block.Block bukkitBlock) {
        return bukkitBlock.getType() == Material.ENDER_CHEST || bukkitBlock.getState() instanceof org.bukkit.block.Chest;
    }

    @Override
    public boolean isAnyContainerNeeded(@NotNull Player bukkitPlayer, @NotNull org.bukkit.block.Block bukkitBlock) {

        World world = PlayerDataManager.getHandle(bukkitPlayer).world;
        BlockPosition blockPosition = new BlockPosition(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
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

    private boolean isBlockedChest(World world, BlockPosition blockPosition) {
        // For reference, loot at net.minecraft.server.BlockChest
        return world.getType(blockPosition.up()).getBlock().c() || hasOcelotOnTop(world, blockPosition);
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

    @Override
    public boolean activateContainer(@NotNull Player bukkitPlayer, boolean silent, @NotNull org.bukkit.block.Block bukkitBlock) {

        EntityPlayer player = PlayerDataManager.getHandle(bukkitPlayer);

        // Silent ender chest is pretty much API-only
        if (silent && bukkitBlock.getType() == Material.ENDER_CHEST) {
            bukkitPlayer.openInventory(bukkitPlayer.getEnderChest());
            player.b(StatisticList.V);
            return true;
        }

        World world = player.world;
        BlockPosition blockPosition = new BlockPosition(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        Object tile = world.getTileEntity(blockPosition);

        if (tile == null) {
            return false;
        }

        if (tile instanceof TileEntityEnderChest) {
            // Anychest ender chest. See net.minecraft.server.BlockEnderChest
            InventoryEnderChest enderChest = player.getEnderChest();
            enderChest.a((TileEntityEnderChest) tile);
            player.openContainer(enderChest);
            player.b(StatisticList.V);
            return true;
        }

        if (!(tile instanceof ITileInventory)) {
            return false;
        }

        ITileInventory tileInventory = (ITileInventory) tile;
        Block block = world.getType(blockPosition).getBlock();

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

                if ((localEnumDirection == EnumDirection.WEST) || (localEnumDirection == EnumDirection.NORTH)) {
                    tileInventory = new InventoryLargeChest("container.chestDouble",
                            (TileEntityChest) localTileEntity, tileInventory);
                } else {
                    tileInventory = new InventoryLargeChest("container.chestDouble",
                            tileInventory, (TileEntityChest) localTileEntity);
                }
                break;
            }

            BlockChest blockChest = (BlockChest) block;
            if (blockChest.b == 0) {
                player.b(StatisticList.aa);
            } else if (blockChest.b == 1) {
                player.b(StatisticList.U);
            }
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
