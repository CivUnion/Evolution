package com.github.longboyy.evolution.traits;

import com.github.longboyy.evolution.traits.configs.TraitConfig;
import com.google.common.collect.ImmutableSet;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;

public abstract class ListenerTrait<T extends TraitConfig> extends Trait<T> implements Listener {
	public ListenerTrait(String identifier, TraitCategory category, ImmutableSet<EntityType> allowedTypes) {
		super(identifier, category, allowedTypes);
	}
}
