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

package com.lishid.openinv.internal.v1_9_R2;

import java.lang.reflect.Field;

import com.lishid.openinv.internal.ISpecialEnderChest;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

// Volatile
import net.minecraft.server.v1_9_R2.IInventory;
import net.minecraft.server.v1_9_R2.InventoryEnderChest;
import net.minecraft.server.v1_9_R2.InventorySubcontainer;

import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftInventory;

public class SpecialEnderChest extends InventorySubcontainer implements IInventory, ISpecialEnderChest {

    private final InventoryEnderChest enderChest;
    private final CraftInventory inventory = new CraftInventory(this);
    private boolean playerOnline = false;

    public SpecialEnderChest(Player player, Boolean online) {
        super(((CraftPlayer) player).getHandle().getEnderChest().getName(),
                ((CraftPlayer) player).getHandle().getEnderChest().hasCustomName(),
                ((CraftPlayer) player).getHandle().getEnderChest().getSize());
        CraftPlayer craftPlayer = (CraftPlayer) player;
        this.enderChest = craftPlayer.getHandle().getEnderChest();
        this.bukkitOwner = craftPlayer;
        this.items = enderChest.getContents();
    }

    @Override
    public Inventory getBukkitInventory() {
        return inventory;
    }

    @Override
    public void setPlayerOnline(Player player) {
        if (!playerOnline) {
            try {
                this.bukkitOwner = player;
                CraftPlayer craftPlayer = (CraftPlayer) player;
                InventoryEnderChest playerEnderChest = craftPlayer.getHandle().getEnderChest();
                Field field = playerEnderChest.getClass().getField("items");
                field.setAccessible(true);
                field.set(playerEnderChest, this.items);
            } catch (Exception e) {}
            playerOnline = true;
        }
    }

    @Override
    public void setPlayerOffline() {
        playerOnline = false;
    }

    @Override
    public boolean isInUse() {
        return !this.getViewers().isEmpty();
    }

    @Override
    public void update() {
        super.update();
        enderChest.update();
    }

}
