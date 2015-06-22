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

package com.lishid.openinv.internal;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import com.lishid.openinv.OpenInv;

//Volatile
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.*;
import org.bukkit.craftbukkit.v1_8_R3.inventory.*;

public class SpecialEnderChest extends InventorySubcontainer {
    private final CraftInventory inventory = new CraftInventory(this);
    private final InventoryEnderChest enderChest;
    private final CraftPlayer owner;
    private boolean playerOnline = false;

    public SpecialEnderChest(Player p, Boolean online) {
        this(p, ((CraftPlayer) p).getHandle().getEnderChest(), online);
    }

    public SpecialEnderChest(Player p, InventoryEnderChest enderchest, boolean online) {
        super(enderchest.getName(), enderchest.hasCustomName(), enderchest.getSize());
        this.owner = (CraftPlayer) p;
        this.enderChest = enderchest;
        this.items = enderChest.getContents();
        this.playerOnline = online;
        OpenInv.enderChests.put(owner.getUniqueId(), this);
    }

    private void saveOnExit() {
        if (transaction.isEmpty() && !playerOnline) {
            owner.saveData();
            OpenInv.enderChests.remove(owner.getUniqueId());
        }
    }

    private void linkInventory(InventoryEnderChest inventory) {
        inventory.items = this.items;
    }

    public Inventory getBukkitInventory() {
        return inventory;
    }

    public void playerOnline(Player p) {
        if (!playerOnline) {
            linkInventory(((CraftPlayer) p).getHandle().getEnderChest());
            p.saveData();
            playerOnline = true;
        }
    }

    public void playerOffline() {
        playerOnline = false;
        owner.loadData();
        linkInventory(owner.getHandle().getEnderChest());
        saveOnExit();
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        super.onClose(who);
        saveOnExit();
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
