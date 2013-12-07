/*
 * Copyright (C) 2011-2014 lishid.  All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.openinv;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class OpenInvEntityListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent evt = (EntityDamageByEntityEvent) event;
            Entity attacker = evt.getDamager();
            Entity defender = evt.getEntity();

            if (!(attacker instanceof Player) || !(defender instanceof Player)) {
                return;
            }

            Player player = (Player) attacker;

            if (!(player.getItemInHand().getType().getId() == OpenInv.GetItemOpenInvItem()) || (!OpenInv.GetPlayerItemOpenInvStatus(player.getName())) || !OpenInv.hasPermission(player, "OpenInv.openinv")) {
                return;
            }

            Player target = (Player) defender;
            player.performCommand("openinv " + target.getName());

            evt.setDamage(0);
            evt.setCancelled(true);
        }
    }
}
