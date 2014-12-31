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

package com.lishid.openinv.internal.craftbukkit;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.internal.ISpecialEnderChest;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

//Volatile
import net.minecraft.server.*;
import org.bukkit.craftbukkit.entity.*;
import org.bukkit.craftbukkit.inventory.*;

public class SpecialEnderChest extends InventorySubcontainer implements IInventory, ISpecialEnderChest {
    public List<HumanEntity> transaction = new ArrayList<HumanEntity>();
    public boolean playerOnline = false;
    private CraftPlayer owner;
    private InventoryEnderChest enderChest;
    private int maxStack = MAX_STACK;
    private CraftInventory inventory = new CraftInventory(this);

    public SpecialEnderChest(Player p, Boolean online) {
        super(((CraftPlayer) p).getHandle().getEnderChest().getName(), ((CraftPlayer) p).getHandle().getEnderChest().getSize());
        CraftPlayer player = (CraftPlayer) p;
        this.enderChest = player.getHandle().getEnderChest();
        this.owner = player;
        this.items = enderChest.getContents();
        OpenInv.enderChests.put(owner.getName().toLowerCase(), this);
    }

    public Inventory getBukkitInventory() {
        return inventory;
    }

    public void InventoryRemovalCheck() {
        owner.saveData();
        if (transaction.isEmpty() && !playerOnline) {
            OpenInv.enderChests.remove(owner.getName().toLowerCase());
        }
    }

    public void playerOnline(Player p) {
        if (!playerOnline) {
            try {
                InventoryEnderChest playerEnderChest = ((CraftPlayer) p).getHandle().getEnderChest();
                Field field = playerEnderChest.getClass().getField("items");
                field.setAccessible(true);
                field.set(playerEnderChest, this.items);
            }
            catch (Exception e) {}
            p.saveData();
            playerOnline = true;
        }
    }

    public void playerOffline() {
        playerOnline = false;
    }

    public ItemStack[] getContents() {
        return this.items;
    }

    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
        this.InventoryRemovalCheck();
    }

    public List<HumanEntity> getViewers() {
        return transaction;
    }

    public InventoryHolder getOwner() {
        return this.owner;
    }

    public void setMaxStackSize(int size) {
        maxStack = size;
    }

    public int getMaxStackSize() {
        return maxStack;
    }

    public boolean a(EntityHuman entityhuman) {
        return true;
    }

    public void startOpen() {

    }

    public void f() {

    }

    public void update() {
        enderChest.update();
    }
}
