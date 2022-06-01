package com.github.longboyy.evolution.events;

import com.github.longboyy.evolution.traits.ITrait;
import com.github.longboyy.evolution.traits.TraitEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RemoveTraitEvent extends TraitEntityEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final ITrait trait;

	private boolean cancelled;

	public RemoveTraitEvent(TraitEntity entity, ITrait trait) {
		super(entity);
		this.trait = trait;
	}

	public ITrait getTrait() {
		return trait;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
}
