package com.github.longboyy.evolution.traits;

import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.events.AddTraitEvent;
import com.github.longboyy.evolution.events.ChangeVariationEvent;
import com.github.longboyy.evolution.events.RemoveTraitEvent;
import com.github.longboyy.evolution.events.SetTraitsEvent;
import com.github.longboyy.evolution.util.pdc.StringDoubleMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.pdc.extensions.PersistentDataContainerExtensions;
import vg.civcraft.mc.civmodcore.utilities.MoreMath;

import java.util.*;
import java.util.stream.Collectors;

import static vg.civcraft.mc.civmodcore.pdc.extensions.PersistentDataContainerExtensions.getList;

public class TraitEntity {

	@NotNull
	public final LivingEntity entity;

	public TraitEntity(Entity entity) {
		if(!(entity instanceof LivingEntity)){
			throw new IllegalArgumentException("TraitEntity only supports LivingEntity");
		}
		this.entity = (LivingEntity) Objects.requireNonNull(entity);
	}

	public TraitEntity(LivingEntity entity){
		this.entity = Objects.requireNonNull(entity);
	}




	// Spigot setters/getters start

	public PersistentDataContainer getPersistentDataContainer(){
		return this.entity.getPersistentDataContainer();
	}

	public AttributeInstance getAttribute(Attribute attribute){
		return this.entity.getAttribute(attribute);
	}

	public EntityType getType(){
		return this.entity.getType();
	}

	public void damage(double damage){
		this.entity.damage(damage);
	}

	public void setHealth(double health){
		this.entity.setHealth(health);
	}

	public double getHealth(){
		return this.entity.getHealth();
	}

	public String getName(){
		return this.getName();
	}

	public UUID getUniqueId(){
		return this.getUniqueId();
	}

	// Spigot setters/getters end


	// Trait related start

	public boolean applyTrait(ITrait trait, double variation){
		try{
			return trait.applyTrait(this, variation);
		}catch(Exception e){
			return false;
		}
	}

	public void setVariation(ITrait trait, double variation){
		PersistentDataContainer pdc = this.entity.getPersistentDataContainer();

		ChangeVariationEvent cve = new ChangeVariationEvent(this, trait, variation);

		if(cve.callEvent()) {
			variation = cve.getVariation();
			NamespacedKey variationsKey = NamespacedKey.fromString("variations", Evolution.getInstance());
			Map<String, Double> variations = pdc.has(variationsKey)
					? pdc.get(variationsKey, StringDoubleMap.STRING_DOUBLE_MAP) : new HashMap<>();

			variation = MoreMath.clamp(variation, -1D, 1D);
			variations.put(trait.getIdentifier(), variation);
			pdc.set(variationsKey, StringDoubleMap.STRING_DOUBLE_MAP, variations);
		}
	}

	public double getVariation(ITrait trait){
		return trait.getVariation(this);
	}

	public boolean hasTrait(ITrait trait){
		return this.hasTrait(trait, TraitType.ACTIVE) || this.hasTrait(trait, TraitType.INACTIVE);
	}

	public boolean hasTrait(ITrait trait, TraitType type){
		ImmutableSet<ITrait> traits = this.getTraits(type);
		return traits != null && traits.contains(trait);
	}

	public boolean addTrait(ITrait trait, TraitType type){
		Set<ITrait> newTraits = new HashSet<>(this.getTraits(type));
		boolean success = newTraits.add(trait);
		if(success) {
			AddTraitEvent ate = new AddTraitEvent(this, trait, type);
			if(ate.callEvent()) {
				this.setTraits(ImmutableSet.copyOf(newTraits), type);
			}
		}
		return success;
	}

	public boolean removeTrait(ITrait trait){
		PersistentDataContainer pdc = this.entity.getPersistentDataContainer();

		RemoveTraitEvent rte = new RemoveTraitEvent(this, trait);
		if(rte.callEvent()) {
			for (TraitType type : TraitType.values()) {
				if (PersistentDataContainerExtensions.hasList(pdc, type.getKey())) {
					List<String> stringTraits = PersistentDataContainerExtensions.getList(pdc, type.getKey(), PersistentDataType.STRING);
					if (stringTraits.contains(trait.getIdentifier())) {
						stringTraits.remove(trait.getIdentifier());
						PersistentDataContainerExtensions.setList(pdc, type.getKey(), PersistentDataType.STRING, stringTraits);
						return true;
					}
				}
			}
		}

		return false;
	}

	public void setTraits(ImmutableSet<ITrait> traits, TraitType type){
		PersistentDataContainer pdc = this.entity.getPersistentDataContainer();

		SetTraitsEvent ste = new SetTraitsEvent(this, traits, type);
		if(ste.callEvent()){
			List<String> stringTraits = ste.getTraits().stream().map(trait -> trait.getIdentifier()).collect(Collectors.toList());
			PersistentDataContainerExtensions.setList(pdc, type.getKey(), PersistentDataType.STRING, stringTraits);
		}


	}

	public ImmutableMap<ITrait, TraitType> getTraits(){
		Map<ITrait, TraitType> allTraits = new HashMap<>();
		for(TraitType type : TraitType.values()){
			ImmutableSet<ITrait> traits = this.getTraits(type);
			if(traits != null){
				traits.forEach(trait -> allTraits.put(trait, type));
			}
		}

		return ImmutableMap.copyOf(allTraits);
	}

	public ImmutableSet<ITrait> getTraits(TraitCategory category){
		return ImmutableSet.copyOf(this.getTraits().keySet().stream()
				.filter(trait -> trait.getCategory() == category)
				.collect(Collectors.toSet()));
	}

	public ImmutableSet<ITrait> getTraits(TraitType type){
		PersistentDataContainer pdc = this.entity.getPersistentDataContainer();
		if(!PersistentDataContainerExtensions.hasList(pdc, type.getKey())){
			return null;
		}

		List<String> traitsArray = getList(pdc, type.getKey(), PersistentDataType.STRING);

		Set<ITrait> traits = new HashSet<>();
		TraitManager manager = Evolution.getInstance().getTraitManager();
		for(String id : traitsArray){
			ITrait trait = manager.getTrait(id);
			if(trait == null){
				continue;
			}
			traits.add(trait);
		}

		return ImmutableSet.copyOf(traits);
	}

	public TraitType getTraitType(ITrait trait){
		if(!this.hasTrait(trait)){
			return null;
		}

		return this.hasTrait(trait, TraitType.ACTIVE) ? TraitType.ACTIVE : TraitType.INACTIVE;
	}

	// Trait related end

}
