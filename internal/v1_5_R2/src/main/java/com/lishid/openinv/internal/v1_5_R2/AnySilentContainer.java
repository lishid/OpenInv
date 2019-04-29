/*
 * Copyright (C) 2011-2018 lishid. All rights reserved.
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

package com.lishid.openinv.internal.v1_5_R2;

import com.lishid.openinv.internal.IAnySilentContainer;
import net.minecraft.server.v1_5_R2.AxisAlignedBB;
import net.minecraft.server.v1_5_R2.BlockEnderChest;
import net.minecraft.server.v1_5_R2.Container;
import net.minecraft.server.v1_5_R2.EntityOcelot;
import net.minecraft.server.v1_5_R2.EntityPlayer;
import net.minecraft.server.v1_5_R2.IInventory;
import net.minecraft.server.v1_5_R2.InventoryEnderChest;
import net.minecraft.server.v1_5_R2.InventoryLargeChest;
import net.minecraft.server.v1_5_R2.Packet100OpenWindow;
import net.minecraft.server.v1_5_R2.TileEntityChest;
import net.minecraft.server.v1_5_R2.TileEntityEnderChest;
import net.minecraft.server.v1_5_R2.World;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_5_R2.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AnySilentContainer implements IAnySilentContainer {

    @Override
    public boolean isAnySilentContainer(@NotNull Block bukkitBlock) {
        return bukkitBlock.getType() == Material.ENDER_CHEST || bukkitBlock.getState() instanceof org.bukkit.block.Chest;
    }

    @Override
    public boolean isAnyContainerNeeded(@NotNull Player bukkitPlayer, @NotNull Block bukkitBlock) {
        // FOR REFERENCE, LOOK AT net.minecraft.server.BlockChest
        World world = PlayerDataManager.getHandle(bukkitPlayer).world;

        if (bukkitBlock instanceof BlockEnderChest) {
            // Ender chests are not blocked by ocelots.
            return world.t(bukkitBlock.getX(), bukkitBlock.getY() + 1, bukkitBlock.getZ());
        }

        // If block or ocelot on top
        if (isBlockedChest(world, bukkitBlock.getX(), bukkitBlock.getY() + 1, bukkitBlock.getZ())) {
            return true;
        }

        int id = world.getTypeId(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());

        // If block next to chest is chest and has a block or ocelot on top
        if (world.getTypeId(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ() + 1) == id) {
            return isBlockedChest(world, bukkitBlock.getX(), bukkitBlock.getY() + 1, bukkitBlock.getZ() + 1);
        } else if(world.getTypeId(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ() - 1) == id) {
            return isBlockedChest(world, bukkitBlock.getX(), bukkitBlock.getY() + 1, bukkitBlock.getZ() - 1);
        } else if (world.getTypeId(bukkitBlock.getX() + 1, bukkitBlock.getY(), bukkitBlock.getZ()) == id) {
            return isBlockedChest(world, bukkitBlock.getX() + 1, bukkitBlock.getY() + 1, bukkitBlock.getZ());
        } else if (world.getTypeId(bukkitBlock.getX() - 1, bukkitBlock.getY(), bukkitBlock.getZ()) == id) {
            return isBlockedChest(world, bukkitBlock.getX() - 1, bukkitBlock.getY() + 1, bukkitBlock.getZ());
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
    public boolean activateContainer(@NotNull Player bukkitPlayer, boolean silent, @NotNull Block bukkitBlock) {

        EntityPlayer player = PlayerDataManager.getHandle(bukkitPlayer);

        // Silent ender chest is API-only
        if (silent && bukkitBlock.getType() == Material.ENDER_CHEST) {
            bukkitPlayer.openInventory(bukkitPlayer.getEnderChest());
            return true;
        }

        World world = player.world;
        Object tile = world.getTileEntity(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());

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
        int id = world.getTypeId(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());

        if (world.getTypeId(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ() + 1) == id) {
            inventory = new InventoryLargeChest("container.chestDouble", inventory, (TileEntityChest) world.getTileEntity(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ() + 1));
        } else if (world.getTypeId(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ() - 1) == id) {
            inventory = new InventoryLargeChest("container.chestDouble", (TileEntityChest) world.getTileEntity(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ() - 1), inventory);
        } else if (world.getTypeId(bukkitBlock.getX() + 1, bukkitBlock.getY(), bukkitBlock.getZ()) == id) {
            inventory = new InventoryLargeChest("container.chestDouble", inventory, (TileEntityChest) world.getTileEntity(bukkitBlock.getX() + 1, bukkitBlock.getY(), bukkitBlock.getZ()));
        } else if (world.getTypeId(bukkitBlock.getX() - 1, bukkitBlock.getY(), bukkitBlock.getZ()) == id) {
            inventory = new InventoryLargeChest("container.chestDouble", (TileEntityChest) world.getTileEntity(bukkitBlock.getX() - 1, bukkitBlock.getY(), bukkitBlock.getZ()), inventory);
        }

        // AnyChest only
        if (!silent) {
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
            player.playerConnection.sendPacket(new Packet100OpenWindow(windowId, 0, inventory.getName(), inventory.getSize(), true));
            player.activeContainer = container;
            player.activeContainer.windowId = windowId;
            player.activeContainer.addSlotListener(player);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            bukkitPlayer.sendMessage(ChatColor.RED + "Error while sending silent container.");
            return false;
        }
    }

    @Override
    public void deactivateContainer(@NotNull final Player bukkitPlayer) {}

}
