package com.github.longboyy.evolution.traits;

import com.google.common.collect.ImmutableSet;
import org.bukkit.entity.LivingEntity;

import java.util.Objects;

public class TraitEntity {

	private final LivingEntity entity;

	public TraitEntity(LivingEntity entity){
		Objects.requireNonNull(entity);
		this.entity = entity;
	}

}
