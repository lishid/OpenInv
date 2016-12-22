package com.lishid.openinv.commands;

import com.lishid.openinv.OpenInv;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Command adding the ability to search online players' inventories for enchantments of a specific
 * type at or above the level specified.
 * 
 * @author Jikoo
 */
public class SearchEnchantPluginCommand implements CommandExecutor {

    private final OpenInv plugin;

    public SearchEnchantPluginCommand(OpenInv plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        Enchantment enchant = null;
        int level = 0;

        for (String argument : args) {
            Enchantment localEnchant = Enchantment.getByName(argument.toUpperCase());
            if (localEnchant != null) {
                enchant = localEnchant;
                continue;
            }
            try {
                level = Integer.parseInt(argument);
            } catch (NumberFormatException ignored) {}
        }

        // Arguments not set correctly
        if (level == 0 && enchant == null) {
            return false;
        }

        StringBuilder players = new StringBuilder();
        for (Player player : plugin.getOnlinePlayers()) {
            boolean flagInventory = containsEnchantment(player.getInventory(), enchant, level);
            boolean flagEnder = containsEnchantment(player.getEnderChest(), enchant, level);

            // No matches, continue
            if (!flagInventory && !flagEnder) {
                continue;
            }

            // Matches, append details
            players.append(player.getName()).append(" (");
            if (flagInventory) {
                players.append("inv");
            }
            if (flagEnder) {
                if (flagInventory) {
                    players.append(',');
                }
                players.append("ender");
            }
            players.append("), ");
        }

        if (players.length() > 0) {
            // Matches found, delete trailing comma and space
            players.delete(players.length() - 2, players.length());
        } else {
            sender.sendMessage("No players found with " + (enchant == null ? "any enchant" : enchant.getName())
                    + " of level " + level + " or higher.");
            return true;
        }

        sender.sendMessage("Players: " + players.toString());
        return true;
    }

    private boolean containsEnchantment(Inventory inventory, Enchantment enchant, int minLevel) {
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            if (enchant != null) {
                if (item.containsEnchantment(enchant) && item.getEnchantmentLevel(enchant) >= minLevel) {
                    return true;
                }
            } else {
                if (!item.hasItemMeta()) {
                    continue;
                }
                ItemMeta meta = item.getItemMeta();
                if (!meta.hasEnchants()) {
                    continue;
                }
                for (int enchLevel : meta.getEnchants().values()) {
                    if (enchLevel >= minLevel) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
