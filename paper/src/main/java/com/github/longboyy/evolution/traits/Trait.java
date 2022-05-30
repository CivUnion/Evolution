package com.github.longboyy.evolution.traits;

import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.util.pdc.StringDoubleMap;
import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public abstract class Trait implements ITrait, Cloneable{

	private static final Random RANDOM = new Random();
	protected final String identifier;
	protected final double weight;
	protected final TraitCategory category;
	protected final ImmutableSet<EntityType> allowedTypes;

	//private final NamespacedKey variationKey;

	public Trait(String identifier, double weight, TraitCategory category, ImmutableSet<EntityType> allowedTypes){
		this.identifier = identifier;
		this.weight = weight;
		this.category = category;
		this.allowedTypes = allowedTypes;

		//this.variationKey = NamespacedKey.fromString(this.getIdentifier()+"_variation", Evolution.getInstance());
	}

	@Override
	public double getVariation(TraitEntity entity) {
		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		NamespacedKey variationsKey = NamespacedKey.fromString("variations", Evolution.getInstance());
		if(pdc.has(variationsKey)) {
			Map<String, Double> variations = pdc.has(variationsKey)
					? pdc.get(variationsKey, StringDoubleMap.STRING_DOUBLE_MAP) : new HashMap<>();
			if(variations.containsKey(this.getIdentifier())){
				return variations.get(this.getIdentifier());
			}
		}
		// Using Random here because Math.random isn't inclusive
		int randNum = RANDOM.nextInt(Integer.MAX_VALUE);
		double multiplier = (randNum/(Integer.MAX_VALUE*0.5D))-1D;
		return this.getMaxVariation()*multiplier;
	}

	@Override
	public boolean applyTrait(TraitEntity entity, double variation) {
		PersistentDataContainer pdc = entity.getPersistentDataContainer();

		NamespacedKey variationsKey = NamespacedKey.fromString("variations", Evolution.getInstance());
		Map<String, Double> variations = pdc.has(variationsKey)
				? pdc.get(variationsKey, StringDoubleMap.STRING_DOUBLE_MAP) : new HashMap<>();

		variations.put(this.getIdentifier(), variation);

		pdc.set(variationsKey, StringDoubleMap.STRING_DOUBLE_MAP, variations);
		return true;
	}

	@Override
	public TextComponent.Builder displayInfo(TraitEntity entity) {

		/*
		TextComponent.Builder traitInfo = Component.text();
		traitInfo.append(Component.text("Variation:"));
		traitInfo.append(Component.space());
		traitInfo.append(Component.text(String.format("%1.5f", this.getVariation(entity))));
		builder.hoverEvent(HoverEvent.showText(traitInfo.asComponent()).value());
		 */


		//builder.append(Component.text("Variation:"));
		//builder.append(Component.space());
		//builder.append(Component.text(String.format("%1.5f", variation), variation >= 0 ? Evolution.SUCCESS_GREEN : Evolution.FAILURE_RED));
		double variation = this.getVariation(entity);

		TextComponent.Builder traitInfo = Component.text();
		traitInfo.append(Component.text("Variation:"));
		traitInfo.append(Component.space());
		traitInfo.append(Component.text(String.format("%1.5f", this.getVariation(entity)), variation >= 0 ? Evolution.SUCCESS_GREEN : Evolution.FAILURE_RED));
		//builder.hoverEvent(HoverEvent.showText(traitInfo.asComponent()).value());

		return traitInfo;
	}

	@Override
	public String getIdentifier(){
		return this.identifier;
	}

	@Override
	public TraitCategory getCategory() {
		return this.category;
	}

	@Override
	public ImmutableSet<EntityType> getAllowedTypes() {
		return this.allowedTypes;
	}

	@Override
	public double getWeight() {
		return this.weight;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Trait trait = (Trait) o;
		return Objects.equals(this.getIdentifier(), trait.getIdentifier());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getIdentifier());
	}
}
