package com.github.longboyy.evolution.traits;

import com.github.longboyy.evolution.Evolution;
import com.google.common.collect.ImmutableSet;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;

public enum TraitType {

	ACTIVE(NamespacedKey.fromString("active_traits", Evolution.getInstance())),
	INACTIVE(NamespacedKey.fromString("inactive_traits", Evolution.getInstance()));

	private NamespacedKey key;

	TraitType(NamespacedKey key){
		this.key = key;
	}

	public NamespacedKey getKey(){
		return this.key;
	}

	public ImmutableSet<ITrait> getTraitsOf(LivingEntity entity){
		if(this == ACTIVE) {
			return Evolution.getInstance().getTraitManager().getActiveTraitsOf(entity);
		}else{
			return Evolution.getInstance().getTraitManager().getInactiveTraitsOf(entity);
		}
	}


}
