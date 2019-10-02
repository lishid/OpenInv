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

package com.lishid.openinv.internal;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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
     * Checks if the container at the given coordinates is blocked.
     *
     * @param player the Player opening the container
     * @param block  the Block
     * @return true if the container is blocked
     */
    boolean isAnyContainerNeeded(@NotNull Player player, @NotNull Block block);

    /**
     * Checks if the given block is a container which can be unblocked or silenced.
     *
     * @param block the BlockState
     * @return true if the Block is a supported container
     */
    boolean isAnySilentContainer(@NotNull Block block);

}
