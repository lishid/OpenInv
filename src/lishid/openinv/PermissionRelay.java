package lishid.openinv;

import org.bukkit.entity.Player;


public class PermissionRelay {
	public static boolean hasPermission(Player player, String permission)
	{
		if(hasPermission2(player, "*") || hasPermission2(player, "OpenInv.*"))
			return true;
		return hasPermission2(player, "OpenInv." + permission);
	}
	
	public static boolean hasPermission2(Player player, String permission)
	{
		if (OpenInv.permissionHandler == null) {
			return player.isOp() ? true : player.hasPermission(permission);
        }else{
        	return OpenInv.permissionHandler.has(player, permission);
        }
	}
}
