package com.github.longboyy.evolution.traits;

import com.github.longboyy.evolution.Evolution;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public enum TraitCategory {

	//Production based traits(bones, leather)
	HUSBANDRY(3, false),
	//Stat based traits(speed, jump)
	UTILITY(3, false),
	//Traits that have no effect other than looking cool
	//COSMETIC(1, true),
	//Traits that apply some kind of impairment or ailment
	ILLNESS(1, false);

	//Common Traits are traits that
	public static final ImmutableList<TraitCategory> COMMON_TRAITS = ImmutableList.of(
			HUSBANDRY,
			UTILITY
	);

	public static final ImmutableList<TraitCategory> UNCOMMON_TRAITS = ImmutableList.of(
			ILLNESS
			//COSMETIC
	);

	public final int maxAllowedTraits;
	public final boolean alwaysActive;

	TraitCategory(int maxAllowedTraits, boolean alwaysActive){
		this.maxAllowedTraits = maxAllowedTraits;
		this.alwaysActive = alwaysActive;
	}

	public ImmutableSet<ITrait> getTraits(){
		return Evolution.getInstance().getTraitManager().getTraits(this);
	}

}
