/*
 * Copyright (C) 2011-2021 lishid. All rights reserved.
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

package com.lishid.openinv.internal.v1_16_R3;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.internal.IAnySilentContainer;
import java.lang.reflect.Field;
import java.util.logging.Level;
import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.BlockBarrel;
import net.minecraft.server.v1_16_R3.BlockChest;
import net.minecraft.server.v1_16_R3.BlockChestTrapped;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.BlockPropertyChestType;
import net.minecraft.server.v1_16_R3.BlockShulkerBox;
import net.minecraft.server.v1_16_R3.ChatMessage;
import net.minecraft.server.v1_16_R3.Container;
import net.minecraft.server.v1_16_R3.ContainerChest;
import net.minecraft.server.v1_16_R3.Containers;
import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EnumGamemode;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.ITileInventory;
import net.minecraft.server.v1_16_R3.InventoryEnderChest;
import net.minecraft.server.v1_16_R3.InventoryLargeChest;
import net.minecraft.server.v1_16_R3.PlayerInteractManager;
import net.minecraft.server.v1_16_R3.PlayerInventory;
import net.minecraft.server.v1_16_R3.TileEntity;
import net.minecraft.server.v1_16_R3.TileEntityChest;
import net.minecraft.server.v1_16_R3.TileEntityEnderChest;
import net.minecraft.server.v1_16_R3.TileEntityLootable;
import net.minecraft.server.v1_16_R3.TileInventory;
import net.minecraft.server.v1_16_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
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
    public boolean isShulkerIgnoreBoundingBox(org.bukkit.block.Block bukkitBlock) {
        org.bukkit.World bukkitWorld = bukkitBlock.getWorld();
        if (!(bukkitWorld instanceof CraftWorld)) {
            bukkitWorld = Bukkit.getWorld(bukkitWorld.getUID());
        }
        if (!(bukkitWorld instanceof CraftWorld)) {
            Exception exception = new IllegalStateException("AnySilentContainer access attempted on an unknown world!");
            OpenInv.getPlugin(OpenInv.class).getLogger().log(Level.WARNING, exception.getMessage(), exception);
            return false;
        }

        final World world = ((CraftWorld) bukkitWorld).getHandle();
        final BlockPosition blockPosition = new BlockPosition(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        return world.getType(blockPosition).d();
    }

    @Override
    public boolean activateContainer(@NotNull final Player bukkitPlayer, final boolean silentchest,
                                     @NotNull final org.bukkit.block.Block bukkitBlock) {

        // Silent ender chest is API-only
        if (silentchest && bukkitBlock.getType() == Material.ENDER_CHEST) {
            bukkitPlayer.openInventory(bukkitPlayer.getEnderChest());
            bukkitPlayer.incrementStatistic(Statistic.ENDERCHEST_OPENED);
            return true;
        }

        EntityPlayer player = PlayerDataManager.getHandle(bukkitPlayer);

        final World world = player.world;
        final BlockPosition blockPosition = new BlockPosition(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        final TileEntity tile = world.getTileEntity(blockPosition);

        if (tile == null) {
            return false;
        }

        if (tile instanceof TileEntityEnderChest) {
            // Anychest ender chest. See net.minecraft.server.BlockEnderChest
            InventoryEnderChest enderChest = player.getEnderChest();
            enderChest.a((TileEntityEnderChest) tile);
            player.openContainer(new TileInventory((containerCounter, playerInventory, ignored) -> {
                Containers<?> containers = PlayerDataManager.getContainers(enderChest.getSize());
                int rows = enderChest.getSize() / 9;
                return new ContainerChest(containers, containerCounter, playerInventory, enderChest, rows);
            }, new ChatMessage("container.enderchest")));
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

            BlockPropertyChestType chestType = blockData.get(BlockChest.c);

            if (chestType != BlockPropertyChestType.SINGLE) {

                BlockPosition adjacentBlockPosition = blockPosition.shift(BlockChest.h(blockData));
                IBlockData adjacentBlockData = world.getType(adjacentBlockPosition);

                if (adjacentBlockData.getBlock() == block) {

                    BlockPropertyChestType adjacentChestType = adjacentBlockData.get(BlockChest.c);

                    if (adjacentChestType != BlockPropertyChestType.SINGLE && chestType != adjacentChestType
                            && adjacentBlockData.get(BlockChest.FACING) == blockData.get(BlockChest.FACING)) {

                        TileEntity adjacentTile = world.getTileEntity(adjacentBlockPosition);

                        if (adjacentTile instanceof TileEntityChest && tileInventory instanceof  TileEntityChest) {
                            TileEntityChest rightChest = chestType == BlockPropertyChestType.RIGHT ? ((TileEntityChest) tileInventory) : (TileEntityChest) adjacentTile;
                            TileEntityChest leftChest = chestType == BlockPropertyChestType.RIGHT ? (TileEntityChest) adjacentTile : ((TileEntityChest) tileInventory);

                            if (silentchest && (rightChest.lootTable != null || leftChest.lootTable != null)) {
                                OpenInv.getPlugin(OpenInv.class).sendSystemMessage(bukkitPlayer, "messages.error.lootNotGenerated");
                                return false;
                            }

                            tileInventory = new ITileInventory() {
                                public Container createMenu(int containerCounter, PlayerInventory playerInventory, EntityHuman entityHuman) {
                                    leftChest.d(playerInventory.player);
                                    rightChest.d(playerInventory.player);
                                    return ContainerChest.b(containerCounter, playerInventory, new InventoryLargeChest(rightChest, leftChest));
                                }

                                public IChatBaseComponent getScoreboardDisplayName() {
                                    if (leftChest.hasCustomName()) {
                                        return leftChest.getScoreboardDisplayName();
                                    }
                                    if (rightChest.hasCustomName()) {
                                        return rightChest.getScoreboardDisplayName();
                                    }
                                    return new ChatMessage("container.chestDouble");
                                }
                            };
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

        if (block instanceof BlockBarrel) {
            bukkitPlayer.incrementStatistic(Statistic.OPEN_BARREL);
        }

        // AnyChest only - SilentChest not active, container unsupported, or unnecessary.
        if (!silentchest || player.playerInteractManager.getGameMode() == EnumGamemode.SPECTATOR) {
            player.openContainer(tileInventory);
            return true;
        }

        // SilentChest requires access to setting players' gamemode directly.
        if (this.playerInteractManagerGamemode == null) {
            return false;
        }

        if (tile instanceof TileEntityLootable) {
            TileEntityLootable lootable = (TileEntityLootable) tile;
            if (lootable.lootTable != null) {
                OpenInv.getPlugin(OpenInv.class).sendSystemMessage(bukkitPlayer, "messages.error.lootNotGenerated");
                return false;
            }
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
            case BARREL:
                break;
            default:
                return;
        }

        EntityPlayer player = PlayerDataManager.getHandle(bukkitPlayer);

        EnumGamemode gamemode = player.playerInteractManager.getGameMode();
        this.forceGameMode(player, EnumGamemode.SPECTATOR);
        player.activeContainer.b(player);
        player.activeContainer.a(player, false);
        player.activeContainer.transferTo(player.defaultContainer, player.getBukkitEntity());
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
