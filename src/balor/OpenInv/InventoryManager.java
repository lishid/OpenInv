/************************************************************************
 * This file is part of AdminCmd.									
 *																		
 * AdminCmd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by	
 * the Free Software Foundation, either version 3 of the License, or		
 * (at your option) any later version.									
 *																		
 * AdminCmd is distributed in the hope that it will be useful,	
 * but WITHOUT ANY WARRANTY; without even the implied warranty of		
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the			
 * GNU General Public License for more details.							
 *																		
 * You should have received a copy of the GNU General Public License
 * along with AdminCmd.  If not, see <http://www.gnu.org/licenses/>.
 ************************************************************************/
package balor.OpenInv;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.google.common.collect.MapMaker;

/**
 * @author Balor (aka Antoine Aflalo)
 * 
 */
public class InventoryManager {
	public static InventoryManager INSTANCE;
	private final Map<Player, ACPlayerInventory> replacedInv = new MapMaker().makeMap();

	/**
 * 
 */
	private InventoryManager() {
	}

	public static void createInstance() {
		if (INSTANCE == null) {
			INSTANCE = new InventoryManager();
		}
	}

	public void onQuit(final Player p) {
		replacedInv.remove(p);
	}

	void closeOfflineInv(final Player p) {
		onQuit(p);
		p.saveData();
	}

	/**
	 * Open the inventory of an offline player
	 * 
	 * @param sender
	 * @param name
	 * @throws PlayerNotFound
	 * @author lishd {@link https
	 *         ://github.com/lishd/OpenInv/blob/master/src/lishid
	 *         /openinv/commands/OpenInvPluginCommand.java}
	 * @throws WorldNotFoundException 
	 */
	public Player openOfflineInv(final Player sender, final String name, final String world)
			throws PlayerNotFound, WorldNotFoundException {
		Player target = null;
		final HashMap<String, String> replace = new HashMap<String, String>();
		replace.put("player", name);
		// Offline inv here...
		// See if the player has data files

		// Find the player folder
		World worldFound = matchWorld(Bukkit.getWorlds(), world);
		if (worldFound == null)
			throw new WorldNotFoundException(ChatColor.RED + "The World " + ChatColor.GOLD + world
					+ ChatColor.RED + " can't be found.");
		final File playerfolder = new File(worldFound.getWorldFolder(), "players");
		if (!playerfolder.exists()) {
			throw new PlayerNotFound(ChatColor.RED + "The player " + ChatColor.GOLD + name
					+ ChatColor.RED + " can't be found.");
		}

		final String playername = matchUser(Arrays.asList(playerfolder.listFiles()), name);
		if (playername == null) {
			throw new PlayerNotFound(ChatColor.RED + "The player " + ChatColor.GOLD + name
					+ ChatColor.RED + " can't be found.");
		}

		// Create an entity to load the player data
		final MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
		final EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), playername,
				new ItemInWorldManager(server.getWorldServer(0)));
		target = (entity == null) ? null : (Player) entity.getBukkitEntity();
		if (target != null) {
			target.loadData();
		} else {
			throw new PlayerNotFound(ChatColor.RED + "The player " + ChatColor.GOLD + name
					+ ChatColor.RED + " can't be found.");
		}
		openInv(sender, target, true);
		return target;
	}

	/**
	 * Open the inventory of the connected player
	 * 
	 * @param sender
	 *            the user who'll see the inventory
	 * @param target
	 *            player to have his inventory opened
	 */
	public void openInv(final Player sender, final Player target) {
		openInv(sender, target, false);
	}

	private void openInv(final Player sender, final Player target, final boolean offline) {
		//Permissions checks
		if (!sender.hasPermission("OpenInv.override") && target.hasPermission("OpenInv.exempt")) {
            sender.sendMessage(ChatColor.RED + target.getDisplayName() + "'s inventory is protected!");
            return ;
        }
		
		if((!sender.hasPermission("OpenInv.crossworld") && !sender.hasPermission("OpenInv.override")) && 
				target.getWorld() != sender.getWorld()){
			sender.sendMessage(ChatColor.RED + target.getDisplayName() + " is not in your world!");
            return ;
		}
		final ACPlayerInventory inventory = getInventory(target, offline);
		final EntityPlayer eh = ((CraftPlayer) sender).getHandle();
		eh.openContainer(inventory);
	}

	private ACPlayerInventory getInventory(final Player player, final boolean offline) {
		ACPlayerInventory inventory = replacedInv.get(player);
		if (inventory == null) {
			if (offline) {
				inventory = new ACOfflinePlayerInventory(player);
			} else {
				inventory = new ACPlayerInventory(player);
			}
		}
		return inventory;

	}

	private String matchUser(final Collection<File> container, final String search) {
		String found = null;
		if (search == null) {
			return found;
		}
		final String lowerSearch = search.toLowerCase();
		int delta = Integer.MAX_VALUE;
		for (final File file : container) {
			final String filename = file.getName();
			final String str = filename.substring(0, filename.length() - 4);
			if (!str.toLowerCase().startsWith(lowerSearch)) {
				continue;
			}
			final int curDelta = str.length() - lowerSearch.length();
			if (curDelta < delta) {
				found = str;
				delta = curDelta;
			}
			if (curDelta == 0) {
				break;
			}

		}
		return found;

	}

	/**
	 * Search for the given string in the list and return it.
	 * 
	 * @param container
	 * @param search
	 * @return
	 */
	private World matchWorld(final Collection<World> container, final String search) {
		World found = null;
		if (search == null) {
			return found;
		}
		final String lowerSearch = search.toLowerCase();
		int delta = Integer.MAX_VALUE;
		for (final World w : container) {
			String str = w.getName();
			if (str.toLowerCase().startsWith(lowerSearch)) {
				final int curDelta = str.length() - lowerSearch.length();
				if (curDelta < delta) {
					found = w;
					delta = curDelta;
				}
				if (curDelta == 0) {
					break;
				}
			}
		}
		return found;

	}

}
