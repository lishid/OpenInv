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

package com.lishid.openinv.internal.v1_4_5;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.internal.ISpecialPlayerInventory;

//Volatile
import net.minecraft.server.v1_4_5.*;
import org.bukkit.craftbukkit.v1_4_5.entity.*;
import org.bukkit.craftbukkit.v1_4_5.inventory.*;

public class SpecialPlayerInventory extends PlayerInventory implements ISpecialPlayerInventory
{
    CraftPlayer owner;
    public boolean playerOnline = false;
    private ItemStack[] extra = new ItemStack[5];
    private CraftInventory inventory = new CraftInventory(this);
    
    public SpecialPlayerInventory(Player p, Boolean online)
    {
        super(((CraftPlayer) p).getHandle());
        this.owner = ((CraftPlayer) p);
        this.playerOnline = online;
        this.items = player.inventory.items;
        this.armor = player.inventory.armor;
        OpenInv.inventories.put(owner.getName().toLowerCase(), this);
    }
    
    @Override
    public Inventory getBukkitInventory()
    {
        return inventory;
    }
    
    @Override
    public void InventoryRemovalCheck()
    {
        owner.saveData();
        if (transaction.isEmpty() && !playerOnline)
        {
            OpenInv.inventories.remove(owner.getName().toLowerCase());
        }
    }
    
    @Override
    public void PlayerGoOnline(Player player)
    {
        if (!playerOnline)
        {
            CraftPlayer p = (CraftPlayer) player;
            p.getHandle().inventory.items = this.items;
            p.getHandle().inventory.armor = this.armor;
            p.saveData();
            playerOnline = true;
        }
    }
    
    @Override
    public void PlayerGoOffline()
    {
        playerOnline = false;
        this.InventoryRemovalCheck();
    }
    
    @Override
    public void onClose(CraftHumanEntity who)
    {
        super.onClose(who);
        this.InventoryRemovalCheck();
    }
    
    @Override
    public ItemStack[] getContents()
    {
        ItemStack[] C = new ItemStack[getSize()];
        System.arraycopy(items, 0, C, 0, items.length);
        System.arraycopy(items, 0, C, items.length, armor.length);
        return C;
    }
    
    @Override
    public int getSize()
    {
        return super.getSize() + 5;
    }
    
    @Override
    public ItemStack getItem(int i)
    {
        ItemStack[] is = this.items;
        
        if (i >= is.length)
        {
            i -= is.length;
            is = this.armor;
        }
        else
        {
            i = getReversedItemSlotNum(i);
        }
        
        if (i >= is.length)
        {
            i -= is.length;
            is = this.extra;
        }
        else if (is == this.armor)
        {
            i = getReversedArmorSlotNum(i);
        }
        
        return is[i];
    }
    
    @Override
    public ItemStack splitStack(int i, int j)
    {
        ItemStack[] is = this.items;
        
        if (i >= is.length)
        {
            i -= is.length;
            is = this.armor;
        }
        else
        {
            i = getReversedItemSlotNum(i);
        }
        
        if (i >= is.length)
        {
            i -= is.length;
            is = this.extra;
        }
        else if (is == this.armor)
        {
            i = getReversedArmorSlotNum(i);
        }
        
        if (is[i] != null)
        {
            ItemStack itemstack;
            
            if (is[i].count <= j)
            {
                itemstack = is[i];
                is[i] = null;
                return itemstack;
            }
            else
            {
                itemstack = is[i].a(j);
                if (is[i].count == 0)
                {
                    is[i] = null;
                }
                
                return itemstack;
            }
        }
        else
        {
            return null;
        }
    }
    
    @Override
    public ItemStack splitWithoutUpdate(int i)
    {
        ItemStack[] is = this.items;
        
        if (i >= is.length)
        {
            i -= is.length;
            is = this.armor;
        }
        else
        {
            i = getReversedItemSlotNum(i);
        }
        
        if (i >= is.length)
        {
            i -= is.length;
            is = this.extra;
        }
        else if (is == this.armor)
        {
            i = getReversedArmorSlotNum(i);
        }
        
        if (is[i] != null)
        {
            ItemStack itemstack = is[i];
            
            is[i] = null;
            return itemstack;
        }
        else
        {
            return null;
        }
    }
    
    @Override
    public void setItem(int i, ItemStack itemstack)
    {
        ItemStack[] is = this.items;
        
        if (i >= is.length)
        {
            i -= is.length;
            is = this.armor;
        }
        else
        {
            i = getReversedItemSlotNum(i);
        }
        
        if (i >= is.length)
        {
            i -= is.length;
            is = this.extra;
        }
        else if (is == this.armor)
        {
            i = getReversedArmorSlotNum(i);
        }
        
        // Effects
        if (is == this.extra)
        {
            owner.getHandle().drop(itemstack);
            itemstack = null;
        }
        
        is[i] = itemstack;
        
        owner.getHandle().defaultContainer.b();
    }
    
    private int getReversedItemSlotNum(int i)
    {
        if (i >= 27)
            return i - 27;
        else
            return i + 9;
    }
    
    private int getReversedArmorSlotNum(int i)
    {
        if (i == 0)
            return 3;
        if (i == 1)
            return 2;
        if (i == 2)
            return 1;
        if (i == 3)
            return 0;
        else
            return i;
    }
    
    @Override
    public String getName()
    {
        if (player.name.length() > 16)
        {
            return player.name.substring(0, 16);
        }
        return player.name;
    }
    
    @Override
    public boolean a_(EntityHuman entityhuman)
    {
        return true;
    }
}