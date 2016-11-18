package com.lishid.openinv.internal;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

public interface IAnySilentContainer extends IAnySilentChest {

    /**
     * Checks if the given block is a container which can be unblocked or silenced.
     * 
     * @param block the BlockState
     * @return true if the Block is a supported container
     */
    public boolean isAnySilentContainer(BlockState block);

    /**
     * Opens the container at the given coordinates for the Player.
     * 
     * @param player
     * @param anychest whether compatibility for blocked containers is to be used
     * @param silentchest whether the container's noise is to be silenced
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return true if the container can be opened
     */
    public boolean activateContainer(Player player, boolean anychest, boolean silentchest, int x, int y, int z);

    /**
     * Checks if the container at the given coordinates is blocked.
     * 
     * @param player the Player opening the container
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return true if the container is blocked
     */
    public boolean isAnyContainerNeeded(Player player, int x, int y, int z);

}
