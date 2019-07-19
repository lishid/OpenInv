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

package com.lishid.openinv.internal.v1_14_R1;

import com.lishid.openinv.internal.IAnySilentContainer;
import java.lang.reflect.Field;
import net.minecraft.server.v1_14_R1.Block;
import net.minecraft.server.v1_14_R1.BlockBarrel;
import net.minecraft.server.v1_14_R1.BlockChest;
import net.minecraft.server.v1_14_R1.BlockChestTrapped;
import net.minecraft.server.v1_14_R1.BlockEnderChest;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.BlockPropertyChestType;
import net.minecraft.server.v1_14_R1.BlockShulkerBox;
import net.minecraft.server.v1_14_R1.ChatMessage;
import net.minecraft.server.v1_14_R1.Container;
import net.minecraft.server.v1_14_R1.ContainerChest;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EnumChatFormat;
import net.minecraft.server.v1_14_R1.EnumGamemode;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import net.minecraft.server.v1_14_R1.ITileInventory;
import net.minecraft.server.v1_14_R1.InventoryEnderChest;
import net.minecraft.server.v1_14_R1.InventoryLargeChest;
import net.minecraft.server.v1_14_R1.PlayerInteractManager;
import net.minecraft.server.v1_14_R1.PlayerInventory;
import net.minecraft.server.v1_14_R1.TileEntity;
import net.minecraft.server.v1_14_R1.TileEntityChest;
import net.minecraft.server.v1_14_R1.TileEntityEnderChest;
import net.minecraft.server.v1_14_R1.TileEntityLootable;
import net.minecraft.server.v1_14_R1.TileInventory;
import net.minecraft.server.v1_14_R1.World;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Barrel;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.EnderChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.util.BoundingBox;
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
                || state instanceof org.bukkit.block.ShulkerBox
                || state instanceof org.bukkit.block.Barrel;
    }

    @Override
    public boolean isAnyContainerNeeded(@NotNull final Player p, @NotNull final org.bukkit.block.Block block) {
        BlockState blockState = block.getState();

        // Barrels do not require AnyContainer.
        if (blockState instanceof Barrel) {
            return false;
        }

        // Enderchests require a non-occluding block on top to open.
        if (blockState instanceof EnderChest) {
            return block.getRelative(0, 1, 0).getType().isOccluding();
        }

        // Shulker boxes require 1/2 a block clear in the direction they open.
        if (blockState instanceof ShulkerBox) {
            BoundingBox boundingBox = block.getBoundingBox();
            if (boundingBox.getVolume() > 1) {
                // Shulker box is already open.
                return false;
            }

            BlockData blockData = block.getBlockData();
            if (!(blockData instanceof Directional)) {
                // Shouldn't be possible. Just in case, demand AnyChest.
                return true;
            }

            Directional directional = (Directional) blockData;
            BlockFace face = directional.getFacing();
            boundingBox.shift(face.getDirection());
            // Return whether or not bounding boxes overlap.
            return block.getRelative(face, 1).getBoundingBox().overlaps(boundingBox);
        }

        if (!(blockState instanceof org.bukkit.block.Chest)) {
            return false;
        }

        if (isBlockedChest(block)) {
            return true;
        }

        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof Chest) || ((Chest) blockData).getType() == Chest.Type.SINGLE) {
            return false;
        }

        Chest chest = (Chest) blockData;
        int ordinal = (chest.getFacing().ordinal() + 4 + (chest.getType() == Chest.Type.RIGHT ? -1 : 1)) % 4;
        BlockFace relativeFace = BlockFace.values()[ordinal];
        org.bukkit.block.Block relative = block.getRelative(relativeFace);

        if (relative.getType() != block.getType()) {
            return false;
        }

        BlockData relativeData = relative.getBlockData();
        if (!(relativeData instanceof Chest)) {
            return false;
        }
        
        Chest relativeChest = (Chest) relativeData;
        if (relativeChest.getFacing() != chest.getFacing()
                || relativeChest.getType() != (chest.getType() == Chest.Type.RIGHT ? Chest.Type.LEFT : Chest.Type.RIGHT)) {
            return false;
        }

        return isBlockedChest(relative);
    }

    private boolean isBlockedChest(org.bukkit.block.Block block) {
        org.bukkit.block.Block relative = block.getRelative(0, 1, 0);
        return relative.getType().isOccluding()
                || block.getWorld().getNearbyEntities(BoundingBox.of(relative), entity -> entity instanceof Cat).size() > 0;
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
            player.openContainer(new TileInventory((containerCounter, playerInventory, ignored) ->
                    ContainerChest.a(containerCounter, playerInventory, enderChest), BlockEnderChest.d));
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

                BlockPosition adjacentBlockPosition = blockPosition.shift(BlockChest.j(blockData));
                IBlockData adjacentBlockData = world.getType(adjacentBlockPosition);

                if (adjacentBlockData.getBlock() == block) {

                    BlockPropertyChestType adjacentChestType = adjacentBlockData.get(BlockChest.b);

                    if (adjacentChestType != BlockPropertyChestType.SINGLE && chestType != adjacentChestType
                            && adjacentBlockData.get(BlockChest.FACING) == blockData.get(BlockChest.FACING)) {

                        TileEntity adjacentTile = world.getTileEntity(adjacentBlockPosition);

                        if (adjacentTile instanceof TileEntityChest && tileInventory instanceof  TileEntityChest) {
                            TileEntityChest rightChest = chestType == BlockPropertyChestType.RIGHT ? ((TileEntityChest) tileInventory) : (TileEntityChest) adjacentTile;
                            TileEntityChest leftChest = chestType == BlockPropertyChestType.RIGHT ? (TileEntityChest) adjacentTile : ((TileEntityChest) tileInventory);

                            if (rightChest.lootTable != null || leftChest.lootTable != null) {
                                player.a(new ChatMessage("Loot not generated! Please disable /silentcontainer.").a(EnumChatFormat.RED), true);
                                return false;
                            }

                            tileInventory = new ITileInventory() {
                                public Container createMenu(int containerCounter, PlayerInventory playerInventory, EntityHuman entityHuman) {
                                    leftChest.d(playerInventory.player);
                                    rightChest.d(playerInventory.player);
                                    return ContainerChest.b(containerCounter, playerInventory, new InventoryLargeChest(rightChest, leftChest));
                                }

                                public IChatBaseComponent getScoreboardDisplayName() {
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
                player.a(new ChatMessage("Loot not generated! Please disable /silentcontainer.").a(EnumChatFormat.RED), true);
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
