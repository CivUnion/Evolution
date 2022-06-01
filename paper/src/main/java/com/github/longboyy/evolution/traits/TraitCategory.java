package com.github.longboyy.evolution.traits;

import com.github.longboyy.evolution.Evolution;
import com.google.common.collect.ImmutableSet;

public enum TraitCategory {
	HUSBANDRY,
	UTILITY,
	ILLNESS;

	public ImmutableSet<ITrait> getTraits(){
		return Evolution.getInstance().getTraitManager().getTraits(this);
	}

}
