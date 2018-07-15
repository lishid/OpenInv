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

package com.lishid.openinv.internal.v1_13_R1;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import com.lishid.openinv.internal.ISpecialEnderChest;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import net.minecraft.server.v1_13_R1.EntityPlayer;
import net.minecraft.server.v1_13_R1.IInventory;
import net.minecraft.server.v1_13_R1.InventoryEnderChest;
import net.minecraft.server.v1_13_R1.InventorySubcontainer;
import net.minecraft.server.v1_13_R1.ItemStack;

import org.bukkit.craftbukkit.v1_13_R1.inventory.CraftInventory;

public class SpecialEnderChest extends InventorySubcontainer
        implements IInventory, ISpecialEnderChest {

    private final InventoryEnderChest enderChest;
    private final CraftInventory inventory = new CraftInventory(this);
    private boolean playerOnline;

    public SpecialEnderChest(final Player player, final Boolean online) {
        super(PlayerDataManager.getHandle(player).getEnderChest().getDisplayName(),
                PlayerDataManager.getHandle(player).getEnderChest().getSize(), player);
        this.playerOnline = online;
        this.enderChest = PlayerDataManager.getHandle(player).getEnderChest();
        this.setItemLists(this, this.enderChest.getContents());
    }

    @Override
    public Inventory getBukkitInventory() {
        return this.inventory;
    }

    @Override
    public boolean isInUse() {
        return !this.getViewers().isEmpty();
    }

    private void setItemLists(final InventorySubcontainer subcontainer, final List<ItemStack> list) {
        try {
            // Prepare to remove final modifier
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            // Access and replace main inventory array
            Field field = InventorySubcontainer.class.getField("items");
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(subcontainer, list);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
                | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setPlayerOffline() {
        this.playerOnline = false;
    }

    @Override
    public void setPlayerOnline(final Player player) {
        if (!this.playerOnline) {
            try {
                EntityPlayer nmsPlayer = PlayerDataManager.getHandle(player);
                this.bukkitOwner = nmsPlayer.getBukkitEntity();
                this.setItemLists(nmsPlayer.getEnderChest(), this.items);
            } catch (Exception e) {}
            this.playerOnline = true;
        }
    }

    @Override
    public void update() {
        super.update();
        this.enderChest.update();
    }

}
