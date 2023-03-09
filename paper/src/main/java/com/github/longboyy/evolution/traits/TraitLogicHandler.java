package com.github.longboyy.evolution.traits;

import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.util.TraitPickerBuilder;
import com.github.longboyy.evolution.util.TraitUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import vg.civcraft.mc.civmodcore.utilities.BiasedRandomPicker;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TraitLogicHandler {

	private static final int TRAITS_PER_CATEGORY = 3;

	/*
	public static void handleEntitySpawn(TraitEntity entity){
		if(entity == null || !entity.getTraits().isEmpty()){
			return;
		}

		ImmutableSet<ITrait> validTraits = Evolution.getInstance().getTraitManager().getTraits(entity.getType());
		if(validTraits.isEmpty()){
			//Evolution.getInstance().info("Attempted to generate traits for entity but valid traits was null");
			return;
		}
	}
	 */

	public static void handleEntitySpawn(TraitEntity entity){
		if(!entity.getTraits().isEmpty()){
			return;
		}

		ImmutableSet<ITrait> validTraits = Evolution.getInstance().getTraitManager().getTraits(entity.getType());
		if(validTraits.isEmpty()){
			//Evolution.getInstance().info("Attempted to generate traits for entity but valid traits was null");
			return;
		}

		Set<ITrait> activeTraits = new HashSet<>();
		Set<ITrait> inactiveTraits = new HashSet<>();

		for(TraitCategory category : TraitCategory.values()){
			if(category == TraitCategory.ILLNESS){
				continue;
			}

			Set<ITrait> traits = validTraits.stream().filter(trait -> trait.getCategory() == category).collect(Collectors.toSet());

			BiasedRandomPicker<ITrait> activePicker = TraitPickerBuilder.builder(ImmutableSet.copyOf(traits), entity).build();
			if(activePicker != null){
				ITrait trait = activePicker.getRandom();
				if(trait != null){
					activeTraits.add(trait);
					traits.remove(trait);
				}
			}

			inactiveTraits.addAll(TraitUtils.generateUniqueTraits(ImmutableSet.copyOf(traits), TRAITS_PER_CATEGORY-1));
		}

		Evolution.getInstance().info(String.format("Generated traits for %s, Active traits: %s, Inactive traits: %s", entity.getType(), activeTraits.size(), inactiveTraits.size()));

		entity.setTraits(ImmutableSet.copyOf(activeTraits), TraitType.ACTIVE);
		entity.setTraits(ImmutableSet.copyOf(inactiveTraits), TraitType.INACTIVE);

		for(Map.Entry<ITrait, TraitType> traitEntry : entity.getTraits().entrySet()){
			ITrait trait = traitEntry.getKey();
			entity.setVariation(trait, entity.getVariation(trait));
			if(traitEntry.getValue() == TraitType.ACTIVE){
				trait.applyTrait(entity, entity.getVariation(trait));
			}
		}

		entity.entity.setPersistent(true);
	}

	public static void handleBreed(TraitEntity child, TraitEntity mother, TraitEntity father){
		ImmutableMap<ITrait, TraitType> motherTraits = mother.getTraits();
		if(motherTraits.isEmpty()){
			handleEntitySpawn(mother);
			motherTraits = mother.getTraits();
		}

		ImmutableMap<ITrait, TraitType> fatherTraits = mother.getTraits();
		if(fatherTraits.isEmpty()){
			handleEntitySpawn(father);
			fatherTraits = father.getTraits();
		}

		Set<ITrait> fullPool = new HashSet<>(motherTraits.keySet());
		fullPool.addAll(fatherTraits.keySet());

		double mutateChance = Evolution.getInstance().getConfigParser().getOption("breedMutateChance", 0.5D);
		if(Math.random() <= mutateChance){
			Set<ITrait> mutateTraits = Evolution.getInstance().getTraitManager().getTraits().stream()
					.filter(fullPool::contains).collect(Collectors.toSet());
			BiasedRandomPicker<ITrait> traitPicker = TraitPickerBuilder.builder(ImmutableSet.copyOf(mutateTraits)).setEntity(child).build();
			if(traitPicker != null){
				ITrait mutateTrait = traitPicker.getRandom();
				if(mutateTrait != null){
					fullPool.add(mutateTrait);
				}
			}
		}

		Set<ITrait> activeTraits = new HashSet<>();
		Set<ITrait> inactiveTraits = new HashSet<>();

		for(TraitCategory category : TraitCategory.values()){
			Set<ITrait> categoryTraits = new HashSet<>(fullPool).stream()
					.filter(trait -> trait.getCategory() == category).collect(Collectors.toSet());

			if(!categoryTraits.isEmpty()){
				BiasedRandomPicker<ITrait> categoryPicker = TraitPickerBuilder.builder(
						ImmutableSet.copyOf(categoryTraits),
						child,
						mother,
						father
				).build();

				if(categoryPicker != null){
					ITrait activeTrait = categoryPicker.getRandom();
					activeTraits.add(activeTrait);
					categoryTraits.remove(activeTrait);
				}

				if(category == TraitCategory.ILLNESS){
					inactiveTraits.addAll(categoryTraits);
				}else{
					inactiveTraits.addAll(TraitUtils.generateUniqueTraits(ImmutableSet.copyOf(categoryTraits), TRAITS_PER_CATEGORY-1));
				}

			}
		}

		child.setTraits(ImmutableSet.copyOf(activeTraits), TraitType.ACTIVE);
		child.setTraits(ImmutableSet.copyOf(inactiveTraits), TraitType.INACTIVE);

		for(ITrait trait : child.getTraits().keySet()){
			double variation = (trait.getVariation(mother)+trait.getVariation(father))*0.5D;
			variation += trait.getVariation(child);
			child.setVariation(trait, variation);
			if(child.getTraitType(trait) == TraitType.ACTIVE){
				trait.applyTrait(child, variation);
			}
		}

		child.entity.setPersistent(true);
	}

}
