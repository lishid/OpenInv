package lishid.openinv.utils;

import org.bukkit.entity.Player;

import lishid.openinv.commands.OpenInvPluginCommand;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.PlayerInventory;

public class PlayerInventoryChest extends PlayerInventory
{
	public boolean Offline = false;
	public Player Opener;
	
	public Player Target;
	public PlayerInventoryChest(PlayerInventory inventory, EntityPlayer entityplayer) {
		super(entityplayer);
		this.armor = inventory.armor;
		this.items = inventory.items;
		this.itemInHandIndex = inventory.itemInHandIndex;
		this.e = inventory.e;
		this.b(inventory.l());
	}

	@Override
	public String getName() {
		if(this.d.name.length() > 16)
			return this.d.name.substring(0, 16);
		else
			return this.d.name;
    }

	@Override
	public boolean a(EntityHuman entityhuman)
	{
		return true;
	}
	
	@Override
	public void g() {
		try{
			Player player = OpenInvPluginCommand.offlineInv.get(this);
			if(player != null)
			{
				player.saveData();
				OpenInvPluginCommand.offlineInv.remove(this);
			}
		}catch(Exception e){}
	}
}