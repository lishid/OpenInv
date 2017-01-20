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

package com.lishid.openinv.internal.v1_7_R2;

import com.lishid.openinv.internal.IAnySilentContainer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_7_R2.AxisAlignedBB;
import net.minecraft.server.v1_7_R2.Block;
import net.minecraft.server.v1_7_R2.BlockEnderChest;
import net.minecraft.server.v1_7_R2.Container;
import net.minecraft.server.v1_7_R2.EntityOcelot;
import net.minecraft.server.v1_7_R2.EntityPlayer;
import net.minecraft.server.v1_7_R2.IInventory;
import net.minecraft.server.v1_7_R2.InventoryEnderChest;
import net.minecraft.server.v1_7_R2.InventoryLargeChest;
import net.minecraft.server.v1_7_R2.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_7_R2.TileEntityChest;
import net.minecraft.server.v1_7_R2.TileEntityEnderChest;
import net.minecraft.server.v1_7_R2.World;

import org.bukkit.craftbukkit.v1_7_R2.event.CraftEventFactory;

public class AnySilentContainer implements IAnySilentContainer {

    @Override
    public boolean isAnySilentContainer(org.bukkit.block.Block block) {
        return block.getType() == Material.ENDER_CHEST || block.getState() instanceof org.bukkit.block.Chest;
    }

    @Override
    public boolean isAnyContainerNeeded(Player p, org.bukkit.block.Block block) {
        // FOR REFERENCE, LOOK AT net.minecraft.server.BlockChest
        EntityPlayer player = PlayerDataManager.getHandle(p);
        World world = player.world;

        if (block instanceof BlockEnderChest) {
            // Ender chests are not blocked by ocelots.
            return world.t(block.getX(), block.getY() + 1, block.getZ());
        }

        // If block or ocelot on top
        if (isBlockedChest(world, block.getX(), block.getY() + 1, block.getZ())) {
            return true;
        }

        int id = Block.b(world.getType(block.getX(), block.getY(), block.getZ()));

        // If block next to chest is chest and has a block or ocelot on top
        if (Block.b(world.getType(block.getX(), block.getY(), block.getZ() + 1)) == id) {
            return isBlockedChest(world, block.getX(), block.getY() + 1, block.getZ() + 1);
        } else if(Block.b(world.getType(block.getX(), block.getY(), block.getZ() - 1)) == id) {
            return isBlockedChest(world, block.getX(), block.getY() + 1, block.getZ() - 1);
        } else if (Block.b(world.getType(block.getX() + 1, block.getY(), block.getZ())) == id) {
            return isBlockedChest(world, block.getX() + 1, block.getY() + 1, block.getZ());
        } else if (Block.b(world.getType(block.getX() - 1, block.getY(), block.getZ())) == id) {
            return isBlockedChest(world, block.getX() - 1, block.getY() + 1, block.getZ());
        }

        return false;
    }

    private boolean isBlockedChest(World world, int x, int y, int z) {
        return world.t(x, y + 1, z) || hasOcelotOnTop(world, x, y, z);
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

    @Override
    public boolean activateContainer(Player p, boolean silentchest, org.bukkit.block.Block block) {

        EntityPlayer player = PlayerDataManager.getHandle(p);

        // Silent ender chest is API-only
        if (silentchest && block.getType() == Material.ENDER_CHEST) {
            p.openInventory(p.getEnderChest());
            return true;
        }

        World world = player.world;
        Object tile = world.getTileEntity(block.getX(), block.getY(), block.getZ());

        if (tile == null) {
            return false;
        }

        if (tile instanceof TileEntityEnderChest) {
            // Anychest ender chest. See net.minecraft.server.BlockEnderChest
            InventoryEnderChest enderChest = player.getEnderChest();
            enderChest.a((TileEntityEnderChest) tile);
            player.openContainer(enderChest);
            return true;
        }

        if (!(tile instanceof IInventory)) {
            return false;
        }

        IInventory inventory = (IInventory) tile;
        int id = Block.b(world.getType(block.getX(), block.getY(), block.getZ()));

        if (Block.b(world.getType(block.getX(), block.getY(), block.getZ() + 1)) == id) {
            inventory = new InventoryLargeChest("container.chestDouble", inventory, (TileEntityChest) world.getTileEntity(block.getX(), block.getY(), block.getZ() + 1));
        } else if(Block.b(world.getType(block.getX(), block.getY(), block.getZ() - 1)) == id) {
            inventory = new InventoryLargeChest("container.chestDouble", (TileEntityChest) world.getTileEntity(block.getX(), block.getY(), block.getZ() - 1), inventory);
        } else if (Block.b(world.getType(block.getX() + 1, block.getY(), block.getZ())) == id) {
            inventory = new InventoryLargeChest("container.chestDouble", inventory, (TileEntityChest) world.getTileEntity(block.getX() + 1, block.getY(), block.getZ()));
        } else if (Block.b(world.getType(block.getX() - 1, block.getY(), block.getZ())) == id) {
            inventory = new InventoryLargeChest("container.chestDouble", (TileEntityChest) world.getTileEntity(block.getX() - 1, block.getY(), block.getZ()), inventory);
        }

        // AnyChest only
        if (!silentchest) {
            player.openContainer(inventory);
            return true;
        }

        // SilentChest
        try {
            // Call InventoryOpenEvent
            Container container = new SilentContainerChest(player.inventory, inventory);
            container = CraftEventFactory.callInventoryOpenEvent(player, container);
            if (container == null) {
                return false;
            }

            // Open window
            int windowId = player.nextContainerCounter();
            player.playerConnection.sendPacket(new PacketPlayOutOpenWindow(windowId, 0, inventory.getInventoryName(), inventory.getSize(), true));
            player.activeContainer = container;
            player.activeContainer.windowId = windowId;
            player.activeContainer.addSlotListener(player);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            p.sendMessage(ChatColor.RED + "Error while sending silent container.");
            return false;
        }
    }

}
