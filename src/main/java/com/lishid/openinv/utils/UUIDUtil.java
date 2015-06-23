package com.lishid.openinv.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class UUIDUtil {
    private static Player getPlayer(String name) {
        Validate.notNull(name, "Name cannot be null");

        Player found = null;
        String lowerName = name.toLowerCase();
        int delta = Integer.MAX_VALUE;

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        for (Player player : players) {
            if (player.getName().toLowerCase().startsWith(lowerName)) {
                int curDelta = player.getName().length() - lowerName.length();
                if (curDelta < delta) {
                    found = player;
                    delta = curDelta;
                }
                if (curDelta == 0) break;
            }
        }

        return found;
    }

    public static UUID getUUIDOf(String name) {
        UUID uuid = null;
        Player player = getPlayer(name);

        if (player != null) {
            // Player was found online
            uuid = player.getUniqueId();
        }
        else {
            // Player was not found online. Fetch their UUID instead
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
        }

        return uuid;
    }
}
