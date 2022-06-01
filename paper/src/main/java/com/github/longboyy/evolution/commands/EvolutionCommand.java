package com.github.longboyy.evolution.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.EvolutionConfigParser;
import com.github.longboyy.evolution.traits.ITrait;
import com.github.longboyy.evolution.traits.TraitEntity;
import com.github.longboyy.evolution.traits.TraitManager;
import com.github.longboyy.evolution.traits.TraitType;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@CommandAlias("evo|evolution")
@CommandPermission("evo.op")
public class EvolutionCommand extends BaseCommand {

	private final Evolution plugin;
	private final TraitManager manager;

	public EvolutionCommand(Evolution plugin){
		this.plugin = plugin;
		this.manager = plugin.getTraitManager();
	}

	@Subcommand("reload")
	@Description("Reloads the config for Evolution")
	public void onReload(Player player){
		EvolutionConfigParser configParser = this.plugin.getConfigParser();
		if(configParser.parse()){
			//ConfigurationSection section = configParser.getConfig();
			//this.manager.parseConfig(section);
		}
	}

	@Subcommand("addtrait|at|add")
	@Syntax("<trait>")
	@Description("Add a trait to the entity you're looking at.")
	@CommandCompletion("@EvoTraits @Boolean")
	public void onAddTrait(Player player, String traitId, boolean active){
		ITrait trait = this.manager.getTrait(traitId);
		if(trait == null){
			player.sendMessage(Component.text("Failed to find trait with that name", Evolution.FAILURE_RED));
			return;
		}

		Entity rawEntity = player.getTargetEntity(5);
		if(!(rawEntity instanceof LivingEntity)){
			player.sendMessage(Component.text("You're not looking at a valid entity!", Evolution.FAILURE_RED));
			return;
		}

		//LivingEntity entity = (LivingEntity) rawEntity;
		TraitEntity entity = new TraitEntity(rawEntity);
		if(entity.hasTrait(trait)){
			player.sendMessage(Component.text("This entity already has that trait!", Evolution.FAILURE_RED));
			return;
		}

		entity.addTrait(trait, active ? TraitType.ACTIVE : TraitType.INACTIVE);
		if(active){
			trait.applyTrait(entity, entity.getVariation(trait));
		}

		/*
		ImmutableMap<ITrait, TraitType> traits = entity.getTraits();
		if(traits == null){
			return;
		}

		TraitType type = active ? TraitType.ACTIVE : TraitType.INACTIVE;

		Set<Map.Entry<ITrait, TraitType>> newTraits = new HashSet<>(traits.entrySet());
		//TraitType traitType = traits.get(trait);

		newTraits.removeIf(entry -> entry.getValue() != type);
		Set<ITrait> finalTraits = newTraits.stream().map(entry -> entry.getKey()).collect(Collectors.toSet());
		finalTraits.add(trait);

		if(active){
			this.manager.setActiveTraitsOf(entity, ImmutableSet.copyOf(finalTraits));
			trait.applyTrait(entity, trait.getVariation(entity));
		}else{
			this.manager.setInactiveTraitsOf(entity, ImmutableSet.copyOf(finalTraits));
		}
		 */
		player.sendMessage(Component.text("Successfully added trait to entity.", Evolution.SUCCESS_GREEN));
	}

	@Subcommand("removetrait|rt|remove")
	@Syntax("<trait>")
	@Description("Remove a trait from the entity you're looking at.")
	@CommandCompletion("@EvoTraits")
	public void onRemoveTrait(Player player, String traitId){
		ITrait trait = this.manager.getTrait(traitId);
		if(trait == null){
			player.sendMessage(Component.text("Failed to find trait with that name", Evolution.FAILURE_RED));
			return;
		}

		Entity rawEntity = player.getTargetEntity(5);
		if(!(rawEntity instanceof LivingEntity)){
			player.sendMessage(Component.text("You're not looking at a valid entity!", Evolution.FAILURE_RED));
			return;
		}

		//LivingEntity entity = (LivingEntity) rawEntity;
		TraitEntity entity = new TraitEntity(rawEntity);
		if(!entity.hasTrait(trait)){
			player.sendMessage(Component.text("This entity does not have that trait!", Evolution.FAILURE_RED));
			return;
		}

		entity.removeTrait(trait);

		/*
		ImmutableMap<ITrait, TraitType> traits = manager.getAllTraitsOf(entity);
		if(traits == null){
			return;
		}

		Set<Map.Entry<ITrait, TraitType>> newTraits = new HashSet<>(traits.entrySet());
		TraitType traitType = traits.get(trait);
		if(traitType == TraitType.ACTIVE){
			newTraits.removeIf(entry -> entry.getValue() != TraitType.ACTIVE || entry.getKey().equals(trait));
			this.manager.setActiveTraitsOf(entity, ImmutableSet.copyOf(newTraits.stream().map(entry -> entry.getKey()).collect(Collectors.toSet())));
		}else{
			newTraits.removeIf(entry -> entry.getValue() != TraitType.INACTIVE || entry.getKey().equals(trait));
			this.manager.setInactiveTraitsOf(entity, ImmutableSet.copyOf(newTraits.stream().map(entry -> entry.getKey()).collect(Collectors.toSet())));
		}
		 */
		player.sendMessage(Component.text("Successfully removed trait from entity.", Evolution.SUCCESS_GREEN));
	}

	@Subcommand("setvariation|sv|variation")
	@Syntax("<trait> [variation=0]")
	@Description("Set the variation of the trait for the entity you're looking at.")
	@CommandCompletion("@EvoTraits @range:-1-1")
	public void onSetVariation(Player player, String traitId, @Default("0") Double variation){
		ITrait trait = this.manager.getTrait(traitId);
		if(trait == null){
			player.sendMessage(Component.text("Failed to find trait with that name", Evolution.FAILURE_RED));
			return;
		}


		Entity rawEntity = player.getTargetEntity(5);
		if(!(rawEntity instanceof LivingEntity)){
			player.sendMessage(Component.text("You're not looking at a valid entity!", Evolution.FAILURE_RED));
			return;
		}

		//LivingEntity entity = (LivingEntity) rawEntity;
		TraitEntity entity = new TraitEntity(rawEntity);
		if(!entity.hasTrait(trait)){
			player.sendMessage(Component.text("This entity does not have that trait!", Evolution.FAILURE_RED));
			return;
		}

		//variation = MoreMath.clamp(variation, -1, 1);
		entity.setVariation(trait, variation);
		if(entity.getTraitType(trait) == TraitType.ACTIVE){
			trait.applyTrait(entity, variation);
		}

		/*
		PersistentDataContainer pdc = entity.getPersistentDataContainer();

		NamespacedKey variationsKey = NamespacedKey.fromString("variations", Evolution.getInstance());
		Map<String, Double> variations = pdc.has(variationsKey)
				? pdc.get(variationsKey, StringDoubleMap.getType()) : new HashMap<>();

		variations.put(trait.getIdentifier(), variation);

		pdc.set(variationsKey, StringDoubleMap.getType(), variations);
		trait.applyTrait(entity, variation);
		 */
		player.sendMessage(Component.text("Successfully set variation of entity.", Evolution.SUCCESS_GREEN));
	}

}
