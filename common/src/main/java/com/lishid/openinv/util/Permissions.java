package com.lishid.openinv.util;

import org.bukkit.permissions.Permissible;

public enum Permissions {

    OPENINV("openinv"),
    OVERRIDE("override"),
    EXEMPT("exempt"),
    CROSSWORLD("crossworld"),
    SILENT("silent"),
    SILENT_DEFAULT("silent", "default"),
    ANYCHEST("anychest"),
    ANY_DEFAULT("any", "default"),
    ENDERCHEST("openender"),
    ENDERCHEST_ALL("openenderall"),
    SEARCH("search"),
    EDITINV("editinv"),
    EDITENDER("editender"),
    OPENSELF("openself");

    private final String[] permission;

    Permissions(String... permissions) {
        this.permission = new String[permissions.length + 1];
        this.permission[0] = "OpenInv";
        System.arraycopy(permissions, 0, this.permission, 1, permissions.length);
    }

    public boolean hasPermission(Permissible permissible) {
        StringBuilder permissionBuilder = new StringBuilder();

        // Support wildcard nodes.
        for (int i = 0; i < permission.length; i++) {
            if (permissible.hasPermission(permissionBuilder.toString() + "*")) {
                return true;
            }
            permissionBuilder.append(permission[i]).append('.');
        }

        // Delete trailing period.
        if (permissionBuilder.length() > 0) {
            permissionBuilder.deleteCharAt(permissionBuilder.length() - 1);
        }

        return permissible.hasPermission(permissionBuilder.toString());
    }

}
