/**
 * Copyright (C) 2011-2012 lishid. All rights reserved.
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
package balor.OpenInv;

import net.minecraft.server.EntityHuman;
import net.minecraft.server.ItemStack;
import net.minecraft.server.PlayerInventory;

import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * @author lishd (Modded by Balor) {@link https://github.com/lishd/OpenInv/}
 * 
 */
public class ACPlayerInventory extends PlayerInventory {

	public final ItemStack[] extra = new ItemStack[5];
	public final Player proprietary;

	/**
	 * @param entityhuman
	 */
	ACPlayerInventory(final Player proprietary) {
		super(((CraftPlayer) proprietary).getHandle());
		this.proprietary = proprietary;
		this.armor = player.inventory.armor;
		this.items = player.inventory.items;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.minecraft.server.PlayerInventory#onClose(org.bukkit.craftbukkit.entity
	 * .CraftHumanEntity)
	 */
	@Override
	public void onClose(final CraftHumanEntity who) {
		super.onClose(who);
		if (transaction.isEmpty() && !proprietary.isOnline()) {
			InventoryManager.INSTANCE.closeOfflineInv(proprietary);
		}
	}

	@Override
	public ItemStack[] getContents() {
		final ItemStack[] C = new ItemStack[getSize()];
		System.arraycopy(items, 0, C, 0, items.length);
		System.arraycopy(armor, 0, C, items.length, armor.length);
		return C;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.server.PlayerInventory#getSize()
	 */
	@Override
	public int getSize() {
		return super.getSize() + 5;
	}

	@Override
	public boolean a(final EntityHuman entityhuman) {
		return true;
	}

	@Override
	public String getName() {
		if (player.name.length() > 16) {
			return player.name.substring(0, 16);
		}
		return player.name;
	}

	private int getReversedItemSlotNum(final int i) {
		if (i >= 27) {
			return i - 27;
		} else {
			return i + 9;
		}
	}

	private int getReversedArmorSlotNum(final int i) {
		if (i == 0) {
			return 3;
		}
		if (i == 1) {
			return 2;
		}
		if (i == 2) {
			return 1;
		}
		if (i == 3) {
			return 0;
		} else {
			return i;
		}
	}

	@Override
	public ItemStack getItem(int i) {
		ItemStack[] is = this.items;

		if (i >= is.length) {
			i -= is.length;
			is = this.armor;
		} else {
			i = getReversedItemSlotNum(i);
		}

		if (i >= is.length) {
			i -= is.length;
			is = this.extra;
		} else if (is == this.armor) {
			i = getReversedArmorSlotNum(i);
		}

		return is[i];
	}

	@Override
	public ItemStack splitStack(int i, final int j) {
		ItemStack[] is = this.items;

		if (i >= is.length) {
			i -= is.length;
			is = this.armor;
		} else {
			i = getReversedItemSlotNum(i);
		}

		if (i >= is.length) {
			i -= is.length;
			is = this.extra;
		} else if (is == this.armor) {
			i = getReversedArmorSlotNum(i);
		}

		if (is[i] != null) {
			ItemStack itemstack;

			if (is[i].count <= j) {
				itemstack = is[i];
				is[i] = null;
				return itemstack;
			} else {
				itemstack = is[i].a(j);
				if (is[i].count == 0) {
					is[i] = null;
				}

				return itemstack;
			}
		} else {
			return null;
		}
	}

	@Override
	public ItemStack splitWithoutUpdate(int i) {
		ItemStack[] is = this.items;

		if (i >= is.length) {
			i -= is.length;
			is = this.armor;
		} else {
			i = getReversedItemSlotNum(i);
		}

		if (i >= is.length) {
			i -= is.length;
			is = this.extra;
		} else if (is == this.armor) {
			i = getReversedArmorSlotNum(i);
		}

		if (is[i] != null) {
			final ItemStack itemstack = is[i];

			is[i] = null;
			return itemstack;
		} else {
			return null;
		}
	}

	@Override
	public void setItem(int i, final ItemStack itemstack) {
		ItemStack[] is = this.items;

		if (i >= is.length) {
			i -= is.length;
			is = this.armor;
		} else {
			i = getReversedItemSlotNum(i);
		}

		if (i >= is.length) {
			i -= is.length;
			is = this.extra;
		} else if (is == this.armor) {
			i = getReversedArmorSlotNum(i);
		}
		is[i] = itemstack;
	}
}
