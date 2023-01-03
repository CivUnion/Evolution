package com.github.longboyy.evolution.events;

import com.github.longboyy.evolution.traits.TraitEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class TraitEntityEvent extends Event {

	//private static final HandlerList handlers = new HandlerList();

	private final TraitEntity entity;

	public TraitEntityEvent(TraitEntity entity){
		this.entity = entity;
	}

	public TraitEntity getEntity(){
		return this.entity;
	}

	/*
	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}
	 */
}
