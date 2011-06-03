package lishid.openinv.utils;

import net.minecraft.server.ContainerPlayer;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.InventoryPlayer;

public class PlayerInventoryChest extends InventoryPlayer
{
	public PlayerInventoryChest(InventoryPlayer inventory) {
		super(inventory.d);
		this.armor = inventory.armor;
		this.items = inventory.items;
		this.itemInHandIndex = inventory.itemInHandIndex;
		this.e = inventory.e;
		this.b(inventory.j());
		inventory.d.defaultContainer = new ContainerPlayer(this, !inventory.d.world.isStatic);
		inventory.d.activeContainer = inventory.d.defaultContainer;
	}

	@Override
	public String getName() {
        return ((EntityPlayer)this.d).displayName;
    }

	@Override
	public boolean a_(EntityHuman entityhuman)
	{
		return true;
	}
}