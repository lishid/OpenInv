/*
 * Copyright (C) 2011-2019 lishid. All rights reserved.
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

package com.lishid.openinv.util;

import org.bukkit.permissions.Permissible;

public enum Permissions {

    OPENINV("openinv"),
    OVERRIDE("override"),
    EXEMPT("exempt"),
    CROSSWORLD("crossworld"),
    SILENT("silent"),
    SILENT_DEFAULT("silent.default"),
    ANYCHEST("anychest"),
    ANY_DEFAULT("any.default"),
    ENDERCHEST("openender"),
    ENDERCHEST_ALL("openenderall"),
    SEARCH("search"),
    EDITINV("editinv"),
    EDITENDER("editender"),
    OPENSELF("openself");

    private final String permission;

    Permissions(String permission) {
        this.permission = "OpenInv." + permission;
    }

    public boolean hasPermission(Permissible permissible) {

        boolean hasPermission = permissible.hasPermission(permission);
        if (hasPermission || permissible.isPermissionSet(permission)) {
            return hasPermission;
        }

        StringBuilder permissionDestroyer = new StringBuilder(permission);
        for (int lastPeriod = permissionDestroyer.lastIndexOf("."); lastPeriod > 0;
                lastPeriod = permissionDestroyer.lastIndexOf(".")) {
            permissionDestroyer.delete(lastPeriod + 1, permissionDestroyer.length()).append('*');

            hasPermission = permissible.hasPermission(permissionDestroyer.toString());
            if (hasPermission || permissible.isPermissionSet(permissionDestroyer.toString())) {
                return hasPermission;
            }

            permissionDestroyer.delete(lastPeriod, permissionDestroyer.length());

        }

        return permissible.hasPermission("*");

    }

}
