package lishid.openinv;

import org.bukkit.event.inventory.InventoryListener;

public class OpenInvInventoryListener extends InventoryListener{
	OpenInv plugin;
	public OpenInvInventoryListener(OpenInv scrap) {
		plugin = scrap;
	}
}
