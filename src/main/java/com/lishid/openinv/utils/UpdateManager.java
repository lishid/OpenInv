package com.lishid.openinv.utils;

import java.io.File;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.utils.Updater.UpdateResult;

public class UpdateManager {
    public Updater updater;

    public void Initialize(OpenInv plugin, File file) {
        updater = new Updater(plugin, 31432, file);

        // Create task to update
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                // Check for updates
                if (OpenInv.GetCheckForUpdates()) {
                    UpdateResult result = updater.update();
                    if (result != UpdateResult.NO_UPDATE) {
                        if (result == UpdateResult.SUCCESS) {
                            OpenInv.log("Update found! Downloaded new version.");
                            OpenInv.log("This behaviour can be disabled in the config.yml");
                        }
                        else {
                            OpenInv.log("Update failed, reason: " + result.toString());
                        }
                    }
                }
            }
        }, 0, 20 * 60 * 1000); // Update every once a while
    }
}
