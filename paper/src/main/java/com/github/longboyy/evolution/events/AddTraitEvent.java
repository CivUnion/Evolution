package com.github.longboyy.evolution.events;

import com.github.longboyy.evolution.traits.ITrait;
import com.github.longboyy.evolution.traits.TraitEntity;
import com.github.longboyy.evolution.traits.TraitType;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AddTraitEvent extends TraitEntityEvent {

	private static final HandlerList handlers = new HandlerList();

	private final ITrait trait;
	private final TraitType type;

	public AddTraitEvent(TraitEntity entity, ITrait trait, TraitType type) {
		super(entity);
		this.trait = trait;
		this.type = type;
	}

	public ITrait getTrait() {
		return trait;
	}

	public TraitType getType() {
		return type;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}
}
