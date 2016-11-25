package com.lishid.openinv.internal;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

@SuppressWarnings("deprecation")
public interface IAnySilentContainer extends IAnySilentChest {

    /**
     * Checks if the given block is a container which can be unblocked or silenced.
     * 
     * @param block the BlockState
     * @return true if the Block is a supported container
     */
    public boolean isAnySilentContainer(Block block);

    /**
     * Checks if the container at the given coordinates is blocked.
     * 
     * @param player the Player opening the container
     * @param block the Block
     * @return true if the container is blocked
     */
    public boolean isAnyContainerNeeded(Player player, Block block);

    /**
     * Opens the container at the given coordinates for the Player. If you do not want blocked
     * containers to open, be sure to check {@link #isAnyContainerNeeded(Player, Block)}
     * first.
     * 
     * @param player
     * @param silentchest whether the container's noise is to be silenced
     * @param block the Block
     * @return true if the container can be opened
     */
    public boolean activateContainer(Player player, boolean silentchest, Block block);

}
