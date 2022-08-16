package com.github.longboyy.evolution.traits.impl;

import com.github.longboyy.evolution.traits.ListenerTrait;
import com.github.longboyy.evolution.traits.TraitCategory;
import com.github.longboyy.evolution.traits.TraitEntity;
import com.google.common.collect.ImmutableSet;
import org.bukkit.entity.EntityType;

public class WoolTrait extends ListenerTrait {
	public WoolTrait() {
		super("wool", TraitCategory.HUSBANDRY, ImmutableSet.copyOf(new EntityType[]{
				EntityType.SHEEP
		}));
	}

	@Override
	public double getMaxVariation() {
		return 0;
	}

	@Override
	public String getPrettyName(TraitEntity entity) {
		return null;
	}

	@Override
	public double getWeight(TraitEntity entity) {
		return 0;
	}
}
