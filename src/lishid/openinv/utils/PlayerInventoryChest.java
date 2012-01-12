package lishid.openinv.utils;

import org.bukkit.entity.Player;

import lishid.openinv.commands.OpenInvPluginCommand;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.IInventory;
import net.minecraft.server.ItemStack;
import net.minecraft.server.PlayerInventory;

public class PlayerInventoryChest implements IInventory
{
    public boolean Offline = false;
    public Player Opener;
    EntityPlayer player;
    public Player Target;
    private ItemStack[] items = new ItemStack[36];
    private ItemStack[] armor = new ItemStack[4];
    private ItemStack[] extra = new ItemStack[5];

    public PlayerInventoryChest(PlayerInventory inventory, EntityPlayer entityplayer)
    {
        player = entityplayer;
        this.items = inventory.items;
        this.armor = inventory.armor;
    }

    public ItemStack[] getContents()
    {
        ItemStack[] C = new ItemStack[getSize()];
        System.arraycopy(items, 0, C, 0, items.length);
        System.arraycopy(items, 0, C, items.length, armor.length);
        return C;
    }

    public int getSize()
    {
        return 45;
    }

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
        else if(is == this.armor)
        {
            i = getReversedArmorSlotNum(i);
        }

        return is[i];
    }

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
        else if(is == this.armor)
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
        else if(is == this.armor)
        {
            i = getReversedArmorSlotNum(i);
        }
        
        //Effects
        if(is == this.extra)
        {
        	if(i == 0)
        	{
        		itemstack.setData(0);
        	}
        }

        is[i] = itemstack;
    }

    private int getReversedItemSlotNum(int i)
    {
        if (i >= 27) return i - 27;
        else return i + 9;
    }

    private int getReversedArmorSlotNum(int i)
    {
        if (i == 0) return 3;
        if (i == 1) return 2;
        if (i == 2) return 1;
        if (i == 3) return 0;
        else return i;
    }

    public String getName()
    {
        if (player.name.length() > 16) return player.name.substring(0, 16);
        return player.name;
    }

    public int getMaxStackSize()
    {
        return 64;
    }

    public boolean a(EntityHuman entityhuman)
    {
        return true;
    }

    public void f()
    {

    }

    public void g()
    {
        try
        {
            PlayerInventoryChest inv = OpenInvPluginCommand.offlineInv.get(this.Target);
            if (inv != null)
            {
                this.Target.saveData();
                OpenInvPluginCommand.offlineInv.remove(this.Target);
            }
        }
        catch (Exception e)
        {}
    }

    public void update()
    {

    }
}