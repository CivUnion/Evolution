package com.github.longboyy.evolution.events;

import com.github.longboyy.evolution.traits.ITrait;
import com.github.longboyy.evolution.traits.TraitEntity;
import com.github.longboyy.evolution.traits.TraitType;
import com.google.common.collect.ImmutableSet;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class SetTraitsEvent extends TraitEntityEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final Set<ITrait> traits;
	private final TraitType type;

	private boolean cancelled = false;

	public SetTraitsEvent(TraitEntity entity, ImmutableSet<ITrait> traits, TraitType type) {
		super(entity);
		this.traits = new HashSet<>(traits);
		this.type = type;
	}

	public Set<ITrait> getTraits(){
		return this.traits;
	}

	public TraitType getType(){
		return this.type;
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
