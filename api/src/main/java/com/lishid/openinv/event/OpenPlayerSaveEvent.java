package com.lishid.openinv.event;

import com.lishid.openinv.internal.ISpecialInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired before OpenInv saves a player's data.
 */
public class OpenPlayerSaveEvent extends Event implements Cancellable {

  private static final HandlerList HANDLERS = new HandlerList();

  private final Player player;
  private final ISpecialInventory inventory;
  private boolean cancelled = false;

  public OpenPlayerSaveEvent(@NotNull Player player, @NotNull ISpecialInventory inventory) {
    this.player = player;
    this.inventory = inventory;
  }

  /**
   * Get the {@link Player} whose data is being saved.
   *
   * @return player the Player whose data is being saved
   */
  public @NotNull Player getPlayer() {
    return player;
  }

  /**
   * Get the {@link ISpecialInventory} that triggered the save by being closed.
   *
   * @return the special inventory
   */
  public @NotNull ISpecialInventory getInventory() {
    return inventory;
  }

  /**
   * Get whether the event is cancelled.
   *
   * @return true if the event is cancelled
   */
  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  /**
   * Set whether the event is cancelled.
   *
   * @param cancel whether the event is cancelled
   */
  @Override
  public void setCancelled(boolean cancel) {
    this.cancelled = cancel;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

}
