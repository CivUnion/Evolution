package com.github.longboyy.evolution.events;

import com.github.longboyy.evolution.traits.TraitEntity;
import org.bukkit.event.Event;

public abstract class TraitEntityEvent extends Event {

	private final TraitEntity entity;

	public TraitEntityEvent(TraitEntity entity){
		this.entity = entity;
	}

	public TraitEntity getEntity(){
		return this.entity;
	}

}
