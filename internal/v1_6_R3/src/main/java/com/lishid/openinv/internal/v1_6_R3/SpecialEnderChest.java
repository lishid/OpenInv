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

package com.lishid.openinv.internal.v1_6_R3;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.lishid.openinv.internal.ISpecialEnderChest;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import net.minecraft.server.v1_6_R3.EntityPlayer;
import net.minecraft.server.v1_6_R3.IInventory;
import net.minecraft.server.v1_6_R3.InventoryEnderChest;
import net.minecraft.server.v1_6_R3.InventorySubcontainer;
import net.minecraft.server.v1_6_R3.ItemStack;

import org.bukkit.craftbukkit.v1_6_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_6_R3.inventory.CraftInventory;

public class SpecialEnderChest extends InventorySubcontainer implements IInventory, ISpecialEnderChest {

    private final InventoryEnderChest enderChest;
    private final CraftInventory inventory = new CraftInventory(this);
    private final List<HumanEntity> transaction = new ArrayList<HumanEntity>();
    private boolean playerOnline = false;
    private CraftPlayer owner;
    private int maxStack = MAX_STACK;

    public SpecialEnderChest(Player player, Boolean online) {
        super(PlayerDataManager.getHandle(player).getEnderChest().getName(),
                PlayerDataManager.getHandle(player).getEnderChest().c(),
                PlayerDataManager.getHandle(player).getEnderChest().getSize());
        EntityPlayer nmsPlayer = PlayerDataManager.getHandle(player);
        this.enderChest = nmsPlayer.getEnderChest();
        this.owner = nmsPlayer.getBukkitEntity();
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
                EntityPlayer nmsPlayer = PlayerDataManager.getHandle(player);
                this.owner = nmsPlayer.getBukkitEntity();
                InventoryEnderChest playerEnderChest = nmsPlayer.getEnderChest();
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
    public ItemStack[] getContents() {
        return this.items;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public InventoryHolder getOwner() {
        return this.owner;
    }

    @Override
    public void setMaxStackSize(int size) {
        maxStack = size;
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

    @Override
    public void update() {
        super.update();
        enderChest.update();
    }

}
