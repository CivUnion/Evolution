package com.github.longboyy.evolution.events;

import com.github.longboyy.evolution.traits.ITrait;
import com.github.longboyy.evolution.traits.Trait;
import com.github.longboyy.evolution.traits.TraitEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.utilities.MoreMath;

public class ApplyTraitEvent extends TraitEntityEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final ITrait trait;

	private double variation;
	private boolean cancelled = false;

	public ApplyTraitEvent(TraitEntity entity, ITrait trait, double variation) {
		super(entity);
		this.trait = trait;
		this.variation = variation;
	}

	public ITrait getTrait(){
		return trait;
	}

	public void setVariation(double variation){
		this.variation = MoreMath.clamp(variation, -1D, 1D);
	}

	public double getVariation(){
		return variation;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
}
