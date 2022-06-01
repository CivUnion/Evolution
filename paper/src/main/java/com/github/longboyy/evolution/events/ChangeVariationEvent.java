package com.github.longboyy.evolution.events;

import com.github.longboyy.evolution.traits.ITrait;
import com.github.longboyy.evolution.traits.TraitEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ChangeVariationEvent extends TraitEntityEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final ITrait trait;
	private double variation;

	private boolean cancelled;

	public ChangeVariationEvent(TraitEntity entity, ITrait trait, double variation) {
		super(entity);
		this.trait = trait;
		this.variation = variation;
	}

	public ITrait getTrait(){
		return this.trait;
	}

	public double getVariation(){
		return this.variation;
	}

	public void setVariation(double variation){
		this.variation = variation;
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
