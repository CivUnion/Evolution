package com.github.longboyy.evolution.traits;

import com.github.longboyy.evolution.Evolution;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Keyed;

public enum TraitCategory {
	HUSBANDRY,
	UTILITY,
	ILLNESS;

	public ImmutableSet<ITrait> getTraits(){
		return Evolution.getInstance().getTraitManager().getTraitsByCategory(this);
	}

}
