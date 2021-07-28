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

package com.lishid.openinv.internal.v1_17_R1;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.internal.IAnySilentContainer;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerInteractManager;
import net.minecraft.world.ITileInventory;
import net.minecraft.world.InventoryLargeChest;
import net.minecraft.world.TileInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.ContainerChest;
import net.minecraft.world.inventory.Containers;
import net.minecraft.world.inventory.InventoryEnderChest;
import net.minecraft.world.level.EnumGamemode;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockBarrel;
import net.minecraft.world.level.block.BlockChest;
import net.minecraft.world.level.block.BlockChestTrapped;
import net.minecraft.world.level.block.BlockShulkerBox;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityChest;
import net.minecraft.world.level.block.entity.TileEntityEnderChest;
import net.minecraft.world.level.block.entity.TileEntityLootable;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertyChestType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

public class AnySilentContainer implements IAnySilentContainer {

    private Field playerInteractManagerGamemode;

    public AnySilentContainer() {
        try {
            this.playerInteractManagerGamemode = PlayerInteractManager.class.getDeclaredField("b");
            this.playerInteractManagerGamemode.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException e) {
            Logger logger = OpenInv.getPlugin(OpenInv.class).getLogger();
            logger.warning("Unable to directly write player gamemode! SilentChest will fail.");
            logger.log(Level.WARNING, "Error obtaining gamemode field", e);
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
        // isLargeVoxelShape
        return world.getType(blockPosition).d();
    }

    @Override
    public boolean activateContainer(
            @NotNull final Player bukkitPlayer,
            final boolean silentchest,
            @NotNull final org.bukkit.block.Block bukkitBlock) {

        // Silent ender chest is API-only
        if (silentchest && bukkitBlock.getType() == Material.ENDER_CHEST) {
            bukkitPlayer.openInventory(bukkitPlayer.getEnderChest());
            bukkitPlayer.incrementStatistic(Statistic.ENDERCHEST_OPENED);
            return true;
        }

        EntityPlayer player = PlayerDataManager.getHandle(bukkitPlayer);

        final World world = player.getWorld();
        final BlockPosition blockPosition = new BlockPosition(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        final TileEntity tile = world.getTileEntity(blockPosition);

        if (tile == null) {
            return false;
        }

        if (tile instanceof TileEntityEnderChest) {
            // Anychest ender chest. See net.minecraft.world.level.block.BlockEnderChest
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

        if (!(tile instanceof ITileInventory tileInventory)) {
            return false;
        }

        IBlockData blockData = world.getType(blockPosition);
        Block block = blockData.getBlock();

        if (block instanceof BlockChest) {

            BlockPropertyChestType chestType = blockData.get(BlockChest.c);

            if (chestType != BlockPropertyChestType.a) {

                BlockPosition adjacentBlockPosition = blockPosition.shift(BlockChest.h(blockData));
                IBlockData adjacentBlockData = world.getType(adjacentBlockPosition);

                if (adjacentBlockData.getBlock() == block) {

                    BlockPropertyChestType adjacentChestType = adjacentBlockData.get(BlockChest.c);

                    if (adjacentChestType != BlockPropertyChestType.a && chestType != adjacentChestType
                            && adjacentBlockData.get(BlockChest.b) == blockData.get(BlockChest.b)) {

                        TileEntity adjacentTile = world.getTileEntity(adjacentBlockPosition);

                        if (adjacentTile instanceof TileEntityChest && tileInventory instanceof  TileEntityChest) {
                            TileEntityChest rightChest = chestType == BlockPropertyChestType.c ? ((TileEntityChest) tileInventory) : (TileEntityChest) adjacentTile;
                            TileEntityChest leftChest = chestType == BlockPropertyChestType.c ? (TileEntityChest) adjacentTile : ((TileEntityChest) tileInventory);

                            if (silentchest && (rightChest.g != null || leftChest.g != null)) {
                                OpenInv.getPlugin(OpenInv.class).sendSystemMessage(bukkitPlayer, "messages.error.lootNotGenerated");
                                return false;
                            }

                            tileInventory = new ITileInventory() {
                                public Container createMenu(int containerCounter, PlayerInventory playerInventory, EntityHuman entityHuman) {
                                    leftChest.d(playerInventory.l);
                                    rightChest.d(playerInventory.l);
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
        if (!silentchest || player.d.getGameMode() == EnumGamemode.d) {
            player.openContainer(tileInventory);
            return true;
        }

        // SilentChest requires access to setting players' gamemode directly.
        if (this.playerInteractManagerGamemode == null) {
            return false;
        }

        if (tile instanceof TileEntityLootable lootable) {
            if (lootable.g != null) {
                OpenInv.getPlugin(OpenInv.class).sendSystemMessage(bukkitPlayer, "messages.error.lootNotGenerated");
                return false;
            }
        }

        EnumGamemode gamemode = player.d.getGameMode();
        this.forceGameMode(player, EnumGamemode.d);
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

        // Force game mode change without informing plugins or players.
        EnumGamemode gamemode = player.d.getGameMode();
        this.forceGameMode(player, EnumGamemode.d);

        // See EntityPlayer#closeInventory - can't call or we'd recursively deactivate.
        player.bV.b(player);
        player.bU.a(player.bV);
        player.bV = player.bU;

        // Revert forced game mode.
        this.forceGameMode(player, gamemode);
    }

    private void forceGameMode(final EntityPlayer player, final EnumGamemode gameMode) {
        if (this.playerInteractManagerGamemode == null) {
            // No need to warn repeatedly, error on startup and lack of function should be enough.
            return;
        }
        try {
            this.playerInteractManagerGamemode.setAccessible(true);
            this.playerInteractManagerGamemode.set(player.d, gameMode);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
