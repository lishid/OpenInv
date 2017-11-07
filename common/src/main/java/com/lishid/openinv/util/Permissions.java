package com.lishid.openinv.util;

import org.bukkit.permissions.Permissible;

public enum Permissions {

    OPENINV("openinv"),
    OVERRIDE("override"),
    EXEMPT("exempt"),
    CROSSWORLD("crossworld"),
    SILENT("silent"),
    ANYCHEST("anychest"),
    ENDERCHEST("openender"),
    ENDERCHEST_ALL("openenderall"),
    SEARCH("search"),
    EDITINV("editinv"),
    EDITENDER("editender"),
    OPENSELF("openself");

    private final String[] permission;

    Permissions(String... permission) {
        this.permission = new String[permission.length + 1];
        this.permission[0] = "OpenInv";
        System.arraycopy(permission, 0, permission, 1, permission.length);
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
