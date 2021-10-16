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

package com.lishid.openinv.internal;

import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.EnderChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public interface IAnySilentContainer {

    /**
     * Opens the container at the given coordinates for the Player. If you do not want blocked
     * containers to open, be sure to check {@link #isAnyContainerNeeded(Player, Block)}
     * first.
     *
     * @param player    the Player opening the container
     * @param silent    whether the container's noise is to be silenced
     * @param block     the Block
     * @return true if the container can be opened
     */
    boolean activateContainer(@NotNull Player player, boolean silent, @NotNull Block block);

    /**
     * Closes the Player's currently open container silently, if necessary.
     *
     * @param player the Player closing a container
     */
    void deactivateContainer(@NotNull Player player);

    /**
     * @deprecated use {@link #isAnyContainerNeeded(Block)}
     * @param player the player opening the container
     * @param block the block
     * @return true if the container is blocked
     */
    @Deprecated
    default boolean isAnyContainerNeeded(@NotNull Player player, @NotNull Block block) {
        return isAnyContainerNeeded(block);
    }

    /**
     * Checks if the container at the given coordinates is blocked.
     *
     * @param block  the Block
     * @return true if the container is blocked
     */
    default boolean isAnyContainerNeeded(@NotNull Block block) {
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
            if (isShulkerBlocked((ShulkerBox) blockState)) {
                return true;
            }
        }

        if (!(blockState instanceof org.bukkit.block.Chest)) {
            return false;
        }

        if (isChestBlocked(block)) {
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

        return isChestBlocked(relative);
    }

    boolean isShulkerBlocked(ShulkerBox block);

    /**
     * Determine whether or not a chest is blocked.
     *
     * @param chest the chest block
     * @return true if the chest block cannot be opened under ordinary circumstances
     */
    default boolean isChestBlocked(Block chest) {
        org.bukkit.block.Block relative = chest.getRelative(0, 1, 0);
        return relative.getType().isOccluding()
                || chest.getWorld().getNearbyEntities(BoundingBox.of(relative), entity -> entity instanceof Cat).size() > 0;
    }

    /**
     * Checks if the given block is a container which can be unblocked or silenced.
     *
     * @param block the BlockState
     * @return true if the Block is a supported container
     */
    default boolean isAnySilentContainer(@NotNull Block block) {
        if (block.getType() == Material.ENDER_CHEST) {
            return true;
        }
        BlockState state = block.getState();
        return state instanceof org.bukkit.block.Chest
                || state instanceof org.bukkit.block.ShulkerBox
                || state instanceof org.bukkit.block.Barrel;
    }

}
