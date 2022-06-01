package com.github.longboyy.evolution.traits;

import com.github.longboyy.evolution.Evolution;
import com.google.common.collect.ImmutableSet;
import org.bukkit.NamespacedKey;
public enum TraitType {

	ACTIVE(NamespacedKey.fromString("active_traits", Evolution.getInstance()), "Active"),
	INACTIVE(NamespacedKey.fromString("inactive_traits", Evolution.getInstance()), "Inactive");

	private final NamespacedKey key;
	private final String name;

	TraitType(NamespacedKey key, String name){
		this.key = key;
		this.name = name;
	}

	public NamespacedKey getKey(){
		return this.key;
	}
	public String getName(){
		return this.name;
	}

	public ImmutableSet<ITrait> getTraits(TraitEntity entity){
		return entity.getTraits(this);
	}


}
