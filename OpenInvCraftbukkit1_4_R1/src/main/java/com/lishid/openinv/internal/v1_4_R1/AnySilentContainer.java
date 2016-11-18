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

package com.lishid.openinv.internal.v1_4_R1;

import java.lang.reflect.Field;

import com.lishid.openinv.internal.IAnySilentContainer;

import org.bukkit.ChatColor;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

// Volatile
import net.minecraft.server.v1_4_R1.AxisAlignedBB;
import net.minecraft.server.v1_4_R1.Block;
import net.minecraft.server.v1_4_R1.EntityOcelot;
import net.minecraft.server.v1_4_R1.EntityPlayer;
import net.minecraft.server.v1_4_R1.IInventory;
import net.minecraft.server.v1_4_R1.InventoryLargeChest;
import net.minecraft.server.v1_4_R1.Packet100OpenWindow;
import net.minecraft.server.v1_4_R1.TileEntityChest;
import net.minecraft.server.v1_4_R1.World;

import org.bukkit.craftbukkit.v1_4_R1.entity.CraftPlayer;

public class AnySilentContainer implements IAnySilentContainer {

    @Override
    public boolean isAnySilentContainer(BlockState block) {
        return block instanceof org.bukkit.block.Chest;
    }

    @Override
    public boolean activateContainer(Player p, boolean anychest, boolean silentchest, int x, int y, int z) {
        EntityPlayer player = ((CraftPlayer) p).getHandle();
        World world = player.world;
        Object chest = world.getTileEntity(x, y, z);
        if (chest == null)
            return false;

        if (!anychest && isAnyContainerNeeded(p, x, y, z)) {
            return false;
        }

        if (world.getTypeId(x - 1, y, z) == Block.CHEST.id)
            chest = new InventoryLargeChest("Large chest", (TileEntityChest) world.getTileEntity(x - 1, y, z), (IInventory) chest);
        if (world.getTypeId(x + 1, y, z) == Block.CHEST.id)
            chest = new InventoryLargeChest("Large chest", (IInventory) chest, (TileEntityChest) world.getTileEntity(x + 1, y, z));
        if (world.getTypeId(x, y, z - 1) == Block.CHEST.id)
            chest = new InventoryLargeChest("Large chest", (TileEntityChest) world.getTileEntity(x, y, z - 1), (IInventory) chest);
        if (world.getTypeId(x, y, z + 1) == Block.CHEST.id)
            chest = new InventoryLargeChest("Large chest", (IInventory) chest, (TileEntityChest) world.getTileEntity(x, y, z + 1));

        boolean returnValue = false;
        if (!silentchest) {
            player.openContainer((IInventory) chest);
            returnValue = true;
        } else {
            try {
                int id = 0;
                try {
                    Field windowID = player.getClass().getDeclaredField("containerCounter");
                    windowID.setAccessible(true);
                    id = windowID.getInt(player);
                    id = id % 100 + 1;
                    windowID.setInt(player, id);
                } catch (NoSuchFieldException e) {}

                player.playerConnection.sendPacket(new Packet100OpenWindow(id, 0, ((IInventory) chest).getName(), ((IInventory) chest).getSize()));
                player.activeContainer = new SilentContainerChest(player.inventory, ((IInventory) chest));
                player.activeContainer.windowId = id;
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
        if (world.s(x, y + 1, z) || hasOcelotOnTop(world, x, y, z))
            return true;

        // If block next to chest is chest and has a block or ocelot on top
        return isBlockedChest(world, x - 1, y, z) || isBlockedChest(world, x + 1, y, z)
                || isBlockedChest(world, x, y, z - 1) || isBlockedChest(world, x, y, z + 1);
    }

    private boolean isBlockedChest(World world, int x, int y, int z) {
        if (world.getTypeId(x, y, z) != Block.CHEST.id) {
            return false;
        }

        if (world.s(x, y + 1, z)) {
            return true;
        }

        return hasOcelotOnTop(world, x, y, z);
    }

    private boolean hasOcelotOnTop(World world, int x, int y, int z) {
        for (Object localEntity : world.a(EntityOcelot.class,
                AxisAlignedBB.a().a(x, y + 1, z, x + 1, y + 2, z + 1))) {
            EntityOcelot localEntityOcelot = (EntityOcelot) localEntity;
            if (localEntityOcelot.isSitting()) {
                return true;
            }
        }

        return false;
    }

    /**
     * @deprecated Use {@link #activateContainer(Player, boolean, boolean, int, int, int)}.
     */
    @Deprecated
    @Override
    public boolean activateChest(Player player, boolean anychest, boolean silentchest, int x, int y, int z) {
        return !activateContainer(player, anychest, silentchest, x, y, z);
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
