package com.github.longboyy.evolution.util;

import com.github.longboyy.evolution.traits.ITrait;
import com.github.longboyy.evolution.traits.TraitEntity;
import com.github.longboyy.evolution.traits.TraitType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import vg.civcraft.mc.civmodcore.utilities.BiasedRandomPicker;

import java.util.HashMap;
import java.util.Map;

public class TraitPickerBuilder {

	private ImmutableSet<ITrait> traits;
	private TraitEntity entity;

	private TraitEntity mother;
	private TraitEntity father;

	private TraitPickerBuilder(){
		this(ImmutableSet.copyOf(new ITrait[0]));
	}

	private TraitPickerBuilder(ImmutableSet<ITrait> traits){
		this.traits = traits;
		this.entity = null;
	}

	private TraitPickerBuilder(ImmutableSet<ITrait> traits, TraitEntity entity){
		this.traits = traits;
		this.entity = entity;
	}

	private TraitPickerBuilder(ImmutableSet<ITrait> traits, TraitEntity child, TraitEntity mother, TraitEntity father){
		this(traits, child);
		this.mother = mother;
		this.father = father;
	}

	public static TraitPickerBuilder builder(){
		return new TraitPickerBuilder();
	}

	public static TraitPickerBuilder builder(ImmutableSet<ITrait> traits){
		return new TraitPickerBuilder(traits);
	}

	public static TraitPickerBuilder builder(ImmutableSet<ITrait> traits, TraitEntity entity){
		return new TraitPickerBuilder(traits, entity);
	}

	public static TraitPickerBuilder builder(ImmutableSet<ITrait> traits, TraitEntity child, TraitEntity mother, TraitEntity father){
		return new TraitPickerBuilder(traits, child, mother, father);
	}

	public TraitPickerBuilder setTraits(ImmutableSet<ITrait> traits){
		this.traits = traits;
		return this;
	}

	public TraitPickerBuilder setEntity(TraitEntity entity){
		this.entity = entity;
		return this;
	}

	public TraitPickerBuilder setParents(TraitEntity mother, TraitEntity father){
		this.mother = mother;
		this.father = father;
		return this;
	}

	public BiasedRandomPicker<ITrait> build(){
		if(this.traits == null || this.traits.isEmpty()){
			return null;
		}

		return new BiasedRandomPicker<>(this.generateChanceMap());
	}

	private Map<ITrait, Double> generateChanceMap(){

		Map<ITrait, Double> chances = new HashMap<>();

		if(this.mother != null && this.father != null){
			double totalWeights = 0D;
			for(ITrait trait : this.traits){
				TraitType motherType = this.mother.getTraitType(trait);
				TraitType fatherType = this.father.getTraitType(trait);
				double chance = 1D;
				if(this.mother.hasTrait(trait) && father.hasTrait(trait)){
					if(motherType == TraitType.ACTIVE && fatherType == TraitType.ACTIVE){
						chance = 3D/4D;
					}else if((motherType == TraitType.ACTIVE && fatherType == TraitType.INACTIVE) || (motherType == TraitType.INACTIVE && fatherType == TraitType.ACTIVE)){
						chance = 1D/2D;
					}else{
						chance = 1D/4D;
					}
				}else{
					if(mother.hasTrait(trait)){
						if(motherType == TraitType.ACTIVE){
							chance = 1D/3D;
						}else{
							chance = 1D/8D;
						}
					}else{
						if(fatherType == TraitType.ACTIVE){
							chance = 1D/3D;
						}else{
							chance = 1D/8D;
						}
					}
				}

				double weight = ((trait.getWeight(entity) + trait.getWeight(mother) + trait.getWeight(father))/3) * chance;
				chances.put(trait, weight);
				totalWeights += weight;
			}

			Map<ITrait, Double> realChances = new HashMap<>();
			for(Map.Entry<ITrait, Double> traitChance : chances.entrySet()){
				double realChance = traitChance.getValue()/totalWeights;
				realChances.put(traitChance.getKey(), realChance);
			}

			return realChances;
		}else{
			double totalWeights = this.traits.stream().mapToDouble(trait -> trait.getWeight(this.entity)).sum();

			for(ITrait trait : this.traits){
				double chance = trait.getWeight(this.entity) / totalWeights;
				chances.put(trait, chance);
			}

			return chances;
		}
	}
}
