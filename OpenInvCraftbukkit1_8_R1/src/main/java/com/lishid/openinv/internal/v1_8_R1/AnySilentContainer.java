/*
 * Copyright (C) 2011-2014 lishid.  All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.openinv.internal.v1_8_R1;

import com.lishid.openinv.internal.IAnySilentContainer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

//Volatile
import net.minecraft.server.v1_8_R1.AxisAlignedBB;
import net.minecraft.server.v1_8_R1.Block;
import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.EntityOcelot;
import net.minecraft.server.v1_8_R1.EntityPlayer;
import net.minecraft.server.v1_8_R1.IInventory;
import net.minecraft.server.v1_8_R1.ITileInventory;
import net.minecraft.server.v1_8_R1.InventoryLargeChest;
import net.minecraft.server.v1_8_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_8_R1.TileEntityChest;
import net.minecraft.server.v1_8_R1.World;

import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;

public class AnySilentContainer implements IAnySilentContainer {

    @Override
    public boolean isAnySilentContainer(org.bukkit.block.Block block) {
        return block.getType() == Material.ENDER_CHEST || block.getState() instanceof org.bukkit.block.Chest;
    }

    @Override
    public boolean activateContainer(Player p, boolean silentchest, int x, int y, int z) {
        EntityPlayer player = ((CraftPlayer) p).getHandle();
        World world = player.world;
        Object chest = world.getTileEntity(new BlockPosition(x, y, z));

        if (chest == null) {
            return false;
        }

        int id = Block.getId(world.getType(new BlockPosition(x, y, z)).getBlock());

        if (Block.getId(world.getType(new BlockPosition(x, y, z + 1)).getBlock()) == id) {
            chest = new InventoryLargeChest("Large chest", (ITileInventory) chest, (TileEntityChest) world.getTileEntity(new BlockPosition(x, y, z + 1)));
        } else if (Block.getId(world.getType(new BlockPosition(x, y, z - 1)).getBlock()) == id) {
            chest = new InventoryLargeChest("Large chest", (TileEntityChest) world.getTileEntity(new BlockPosition(x, y, z - 1)), (ITileInventory) chest);
        } else if (Block.getId(world.getType(new BlockPosition(x + 1, y, z)).getBlock()) == id) {
            chest = new InventoryLargeChest("Large chest", (ITileInventory) chest, (TileEntityChest) world.getTileEntity(new BlockPosition(x + 1, y, z)));
        } else if (Block.getId(world.getType(new BlockPosition(x - 1, y, z)).getBlock()) == id) {
            chest = new InventoryLargeChest("Large chest", (TileEntityChest) world.getTileEntity(new BlockPosition(x - 1, y, z)), (ITileInventory) chest);
        }

        boolean returnValue = false;
        if (!silentchest) {
            player.openContainer((IInventory) chest);
            returnValue = true;
        } else {
            try {
                SilentContainerChest silentContainerChest = new SilentContainerChest(player.inventory, ((IInventory) chest), player);
                int windowId = player.nextContainerCounter();
                player.playerConnection.sendPacket(new PacketPlayOutOpenWindow(windowId, "minecraft:chest", ((IInventory) chest).getScoreboardDisplayName(), ((IInventory) chest).getSize()));
                player.activeContainer = silentContainerChest;
                player.activeContainer.windowId = windowId;
                player.activeContainer.addSlotListener(player);
                returnValue = true;
            } catch (Exception e) {
                e.printStackTrace();
                p.sendMessage(ChatColor.RED + "Error while sending silent chest.");
            }
        }

        return returnValue;
    }

    @Override
    public boolean isAnyContainerNeeded(Player p, int x, int y, int z) {
        // FOR REFERENCE, LOOK AT net.minecraft.server.BlockChest
        EntityPlayer player = ((CraftPlayer) p).getHandle();
        World world = player.world;

        // If block or ocelot on top
        if (world.getType(new BlockPosition(x, y + 1, z)).getBlock().c() || hasOcelotOnTop(world, x, y, z))
            return true;

        int id = Block.getId(world.getType(new BlockPosition(x, y, z)).getBlock());

        // If block next to chest is chest and has a block or ocelot on top
        return isBlockedChest(world, id, x - 1, y, z) || isBlockedChest(world, id, x + 1, y, z)
                || isBlockedChest(world, id, x, y, z - 1) || isBlockedChest(world, id, x, y, z + 1);
    }

    private boolean isBlockedChest(World world, int id, int x, int y, int z) {
        if (Block.getId(world.getType(new BlockPosition(x, y, z)).getBlock()) != id) {
            return false;
        }

        if (world.getType(new BlockPosition(x, y + 1, z)).getBlock().c()) {
            return true;
        }

        return hasOcelotOnTop(world, x, y, z);
    }

    private boolean hasOcelotOnTop(World world, int x, int y, int z) {
        for (Object localEntity : world.a(EntityOcelot.class,
                AxisAlignedBB.a(x, y + 1, z, x + 1, y + 2, z + 1))) {
            EntityOcelot localEntityOcelot = (EntityOcelot) localEntity;
            if (localEntityOcelot.isSitting()) {
                return true;
            }
        }

        return false;
    }

    /**
     * @deprecated Use {@link #activateContainer(Player, boolean, int, int, int)}.
     */
    @Deprecated
    @Override
    public boolean activateChest(Player player, boolean anychest, boolean silentchest, int x, int y, int z) {
        return !activateContainer(player, silentchest, x, y, z);
    }

    /**
     * @deprecated Use {@link #isAnyContainerNeeded(Player, int, int, int)}.
     */
    @Deprecated
    @Override
    public boolean isAnyChestNeeded(Player player, int x, int y, int z) {
        return isAnyContainerNeeded(player, x, y, z);
    }

}
