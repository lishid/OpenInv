package lishid.openinv;

import org.bukkit.entity.Player;


public class PermissionRelay {
	public static boolean hasPermission(Player player, String permission)
	{
		if (OpenInv.permissionHandler == null) {
			return player.isOp();
        }else{
        	return OpenInv.permissionHandler.has(player, permission);
        }
	}
}
