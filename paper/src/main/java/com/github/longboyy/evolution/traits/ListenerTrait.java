package com.github.longboyy.evolution.traits;

import com.google.common.collect.ImmutableSet;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;

public abstract class ListenerTrait extends Trait implements Listener {
	public ListenerTrait(String identifier, double weight, TraitCategory category, ImmutableSet<EntityType> allowedTypes) {
		super(identifier, weight, category, allowedTypes);
	}
}
