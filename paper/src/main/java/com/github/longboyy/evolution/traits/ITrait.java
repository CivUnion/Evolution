package com.github.longboyy.evolution.traits;

import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public interface ITrait {

	/**
	 * Unique identifier for this trait
	 */
	String getIdentifier();

	/**
	 * The category that this trait is
	 */
	TraitCategory getCategory();

	/**
	 * The types of entities that are allowed to receive this trait.
	 */
	ImmutableSet<EntityType> getAllowedTypes();

	/**
	 * The maximum amount of variation +/- per generation.
	 */
	double getMaxVariation();

	boolean isEnabled();

	boolean parseConfig(ConfigurationSection section);

	String getPrettyName(TraitEntity entity);

	double getVariation(TraitEntity entity);

	/**
	 * The weight of the trait in the genepool. These weight values are used to decide how rare a trait is.
	 */
	double getWeight(TraitEntity entity);

	/**
	 * Applies the trait to the entity. This process with overwrite it's current value
	 */
	boolean applyTrait(TraitEntity entity, double variation);

	TextComponent.Builder displayInfo(TraitEntity entity);
}
