
package lishid.openinv;

import lishid.openinv.commands.*;
import lishid.openinv.utils.PlayerInventoryChest;

import net.minecraft.server.ContainerPlayer;
import net.minecraft.server.EntityPlayer;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.plugin.Plugin;

/**
 * Open other player's inventory
 *
 * @author lishid
 */
public class OpenInv extends JavaPlugin {
	private final OpenInvPlayerListener playerListener = new OpenInvPlayerListener(this);
	private final OpenInvEntityListener entityListener = new OpenInvEntityListener(this);
    public static PermissionHandler permissionHandler;
    public void onDisable() {
    }
    
    private void setupPermissions() {
        Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");

        if (permissionHandler == null) {
            if (permissionsPlugin != null) {
                permissionHandler = ((Permissions) permissionsPlugin).getHandler();
            } else {
                //log.info("Permission system not detected, defaulting to OP");
            }
        }
    }

    public void onEnable() {

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
		//pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Normal, this);
    	setupPermissions();
        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println("[" + pdfFile.getName() + "] version " + pdfFile.getVersion() + " is enabled!" );

        getCommand("openinv").setExecutor(new OpenInvPluginCommand(this));
        getCommand("searchinv").setExecutor(new SearchInvPluginCommand(this));
        getCommand("toggleopeninv").setExecutor(new OpenInvPluginCommand(this));
    }
    
    public static void ReplaceInv(CraftPlayer player)
    {
    	EntityPlayer entityplayer = player.getHandle();
    	entityplayer.inventory = new PlayerInventoryChest(entityplayer.inventory);
    	entityplayer.defaultContainer = new ContainerPlayer(entityplayer.inventory, !entityplayer.world.isStatic);
    	entityplayer.activeContainer = entityplayer.defaultContainer;
    	player.setHandle(entityplayer);
    }
    
    
}