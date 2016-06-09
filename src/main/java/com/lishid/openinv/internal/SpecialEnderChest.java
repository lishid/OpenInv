/*
 * Copyright (C) 2011-2016 lishid.  All rights reserved.
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

package com.lishid.openinv.internal;

import java.lang.reflect.Field;

import org.bukkit.craftbukkit.v1_10_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import net.minecraft.server.v1_10_R1.InventoryEnderChest;
import net.minecraft.server.v1_10_R1.InventorySubcontainer;
import net.minecraft.server.v1_10_R1.ItemStack;

public class SpecialEnderChest extends InventorySubcontainer {

    private final CraftInventory inventory = new CraftInventory(this);
    private final InventoryEnderChest enderChest;
    private CraftPlayer owner;
    private boolean playerOnline;

    public SpecialEnderChest(Player p, boolean online) {
        this(p, ((CraftPlayer) p).getHandle().getEnderChest(), online);
    }

    public SpecialEnderChest(Player p, InventoryEnderChest enderChest, boolean online) {
        super(enderChest.getName(), enderChest.hasCustomName(), enderChest.getSize());
        this.owner = (CraftPlayer) p;
        this.enderChest = enderChest;
        this.playerOnline = online;
        reflectContents(getClass().getSuperclass(), this, this.enderChest.getContents());
    }

    private void saveOnExit() {
        if (transaction.isEmpty() && !playerOnline) {
            owner.saveData();
        }
    }

    private void reflectContents(Class clazz, InventorySubcontainer enderChest, ItemStack[] items) {
        try {
            Field itemsField = clazz.getDeclaredField("items");
            itemsField.setAccessible(true);
            itemsField.set(enderChest, items);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void linkInventory(InventoryEnderChest inventory) {
        reflectContents(inventory.getClass(), inventory, this.items);
    }

    public Inventory getBukkitInventory() {
        return inventory;
    }

    public boolean inventoryRemovalCheck(boolean save) {
        boolean offline = transaction.isEmpty() && !playerOnline;

        if (offline && save) {
            owner.saveData();
        }

        return offline;
    }

    public void playerOnline(Player p) {
        if (!playerOnline) {
            owner = (CraftPlayer) p;
            linkInventory(((CraftPlayer) p).getHandle().getEnderChest());
            playerOnline = true;
        }
    }

    public boolean playerOffline() {
        playerOnline = false;
        return inventoryRemovalCheck(false);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        super.onClose(who);
        inventoryRemovalCheck(true);
    }

    @Override
    public InventoryHolder getOwner() {
        return this.owner;
    }

    @Override
    public void update() {
        super.update();
        enderChest.update();
    }
}
