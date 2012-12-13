package com.lishid.openinv.utils;

import java.io.File;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.utils.Updater.UpdateResult;
import com.lishid.openinv.utils.Updater.UpdateType;

public class UpdateManager
{
    public Updater updater;
    
    public void Initialize(OpenInv plugin, File file)
    {
        updater = new Updater(plugin, OpenInv.logger, "openinv", file);
        
        // Create task to update
        plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                // Check for updates
                if (OpenInv.GetCheckForUpdates())
                {
                    UpdateResult result = updater.update(UpdateType.DEFAULT);
                    if (result != UpdateResult.NO_UPDATE)
                        OpenInv.log(result.toString());
                }
            }
        }, 0, 20 * 60 * 1000); // Update every once a while
    }
}
