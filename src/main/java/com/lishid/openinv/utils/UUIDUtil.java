package com.lishid.openinv.utils;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;

public class UUIDUtil {
    public static UUID getUUIDOf(String name) {
        UUID uuid = null;

        UUIDFetcher fetcher = new UUIDFetcher(Arrays.asList(name));
        Map<String, UUID> response;

        try {
            response = fetcher.call();
            uuid = response.get(name);
        }
        catch (Exception e) {
            Bukkit.getServer().getLogger().warning("Exception while running UUIDFetcher");
            e.printStackTrace();
        }

        return uuid;
    }
}
