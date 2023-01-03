package com.github.longboyy.evolution.traits;

import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.events.ApplyTraitEvent;
import com.github.longboyy.evolution.traits.configs.TraitConfig;
import com.github.longboyy.evolution.util.pdc.ExtraTypes;
import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataContainer;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public abstract class Trait<T extends TraitConfig> implements ITrait{

	private static final Random RANDOM = new Random();
	protected final String identifier;
	protected final TraitCategory category;
	protected final ImmutableSet<EntityType> allowedTypes;
	protected T config;

	protected boolean enabled = true;

	//private final NamespacedKey variationKey;

	public Trait(String identifier, TraitCategory category, ImmutableSet<EntityType> allowedTypes){
		this.identifier = identifier;
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
					? pdc.get(variationsKey, ExtraTypes.STRING_DOUBLE_MAP) : new HashMap<>();
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
	public double getWeight(TraitEntity entity) {
		return this.config.getWeight();
	}

	@Override
	public boolean applyTrait(TraitEntity entity, double variation) {

		ApplyTraitEvent ate = new ApplyTraitEvent(entity, this, variation);
		if(!ate.callEvent()){
			return false;
		}

		//entity.setVariation(this, ate.getVariation());

		/*
		PersistentDataContainer pdc = entity.getPersistentDataContainer();

		NamespacedKey variationsKey = NamespacedKey.fromString("variations", Evolution.getInstance());
		Map<String, Double> variations = pdc.has(variationsKey)
				? pdc.get(variationsKey, StringDoubleMap.STRING_DOUBLE_MAP) : new HashMap<>();

		variations.put(this.getIdentifier(), variation);

		pdc.set(variationsKey, StringDoubleMap.STRING_DOUBLE_MAP, variations);
		 */
		return true;
	}

	@Override
	public TextComponent.Builder displayInfo(TraitEntity entity) {
		double variation = this.getVariation(entity);

		TextComponent.Builder traitInfo = Component.text();
		traitInfo.append(Component.text("Variation:"));
		traitInfo.append(Component.space());
		traitInfo.append(Component.text(String.format("%1.5f", this.getVariation(entity)), variation >= 0 ? Evolution.SUCCESS_GREEN : Evolution.FAILURE_RED));

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
	public double getMaxVariation() {
		return this.config.getMaxVariationPerGeneration();
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	@Override
	public boolean parseConfig(ConfigurationSection section) {

		Class<? extends TraitConfig> clazz = this.getConfigClass();
		if(clazz == null){
			return false;
		}

		try {
			//this.config = (T) clazz.getConstructor(Void.class).newInstance(null);
			this.config = (T) clazz.getConstructor().newInstance();
			//this.config = (T) clazz.getConstructors()[0].newInstance();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			Evolution.getInstance().severe("Failed to create TraitConfig instance", e);
			return false;
		}

		if(section != null) {
			this.config.parse(section);
			this.enabled = section.getBoolean("enabled", true);
		}else{
			Evolution.getInstance().warning(String.format("Trait '%s' is not configured. Is this a mistake?", this.getIdentifier()));
			this.enabled = true;
		}

		return true;
		//this.config.parse();
	}

	protected abstract Class<T> getConfigClass();

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
