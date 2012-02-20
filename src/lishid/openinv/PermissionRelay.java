/*
 * Copyright (C) 2011 lishid.  All rights reserved.
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
