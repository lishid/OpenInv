/*
 * Copyright (C) 2011-2012 lishid.  All rights reserved.
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

package lishid.openinv.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import lishid.openinv.OpenInv;

import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;

import net.minecraft.server.EntityHuman;
import net.minecraft.server.IInventory;
import net.minecraft.server.InventoryEnderChest;
import net.minecraft.server.InventorySubcontainer;
import net.minecraft.server.ItemStack;

public class OpenInvEnderChest extends InventorySubcontainer implements IInventory
{
    public List<HumanEntity> transaction = new ArrayList<HumanEntity>();
    public boolean playerOnline = false;
    private CraftPlayer owner;
    private InventoryEnderChest enderChest;
    private int maxStack = MAX_STACK;
    
    public OpenInvEnderChest(CraftPlayer player, boolean online)
    {
        super(player.getHandle().getEnderChest().getName(), player.getHandle().getEnderChest().getSize());
        this.enderChest = player.getHandle().getEnderChest();
        this.owner = player;
        this.items = enderChest.getContents();
        this.InventoryRemovalCheck();
    }
    
    public void InventoryRemovalCheck()
    {
        if (transaction.isEmpty() && !playerOnline)
        {
            owner.saveData();
            OpenInv.enderChests.remove(owner.getName().toLowerCase());
        }
    }
    
    public void PlayerGoOnline(CraftPlayer p)
    {
        if (!playerOnline)
        {
            try
            {
                InventoryEnderChest playerEnderChest = p.getHandle().getEnderChest();
                Field field = playerEnderChest.getClass().getField("items");
                field.setAccessible(true);
                field.set(playerEnderChest, this.items);
            }
            catch (Exception e)
            {
            }
            p.saveData();
            playerOnline = true;
        }
    }
    
    public void PlayerGoOffline()
    {
        playerOnline = false;
    }
    
    public ItemStack[] getContents()
    {
        return this.items;
    }
    
    public void onOpen(CraftHumanEntity who)
    {
        transaction.add(who);
    }
    
    public void onClose(CraftHumanEntity who)
    {
        transaction.remove(who);
    }
    
    public List<HumanEntity> getViewers()
    {
        return transaction;
    }
    
    public InventoryHolder getOwner()
    {
        return this.owner;
    }
    
    public void setMaxStackSize(int size)
    {
        maxStack = size;
    }
    
    public int getMaxStackSize()
    {
        return maxStack;
    }
    
    public boolean a(EntityHuman entityhuman)
    {
        return true;
    }
    
    public void startOpen()
    {
        
    }
    
    public void f()
    {
        
    }
    
    public void update()
    {
        enderChest.update();
    }
}
