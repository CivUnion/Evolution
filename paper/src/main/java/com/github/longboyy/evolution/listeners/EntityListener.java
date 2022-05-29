package com.github.longboyy.evolution.listeners;

import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.traits.ITrait;
import com.github.longboyy.evolution.traits.TraitManager;
import com.github.longboyy.evolution.traits.TraitType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.EntitiesLoadEvent;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class EntityListener implements Listener {

	private final Evolution plugin;

	public EntityListener(Evolution plugin){
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntitySpawn(CreatureSpawnEvent event){
		if(event.isCancelled()){
			return;
		}

		TraitManager manager = this.plugin.getTraitManager();
		LivingEntity entity = event.getEntity();

		switch(event.getSpawnReason()){
			case EGG:
			case DISPENSE_EGG:
			case NATURAL:
			case OCELOT_BABY:
			case SPAWNER_EGG:
			case DEFAULT:
				manager.generateTraitsFor(entity);
			default:
				return;
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityBreed(EntityBreedEvent event){
		if(event.isCancelled()){
			return;
		}

		LivingEntity entity = event.getEntity();
		TraitManager manager = this.plugin.getTraitManager();
		manager.generateTraitsFor(entity, event.getMother(), event.getFather());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntitiesLoad(EntitiesLoadEvent event){
		//this.plugin.info("Loading animals in chunk");
		TraitManager manager = this.plugin.getTraitManager();
		//this.plugin.info(String.format("Found %s generic entities in chunk", event.getChunk().getEntities().length));
		Set<LivingEntity> entities = event.getEntities().stream()
				.filter(entity -> entity instanceof LivingEntity)
				.map(entity -> (LivingEntity)entity).collect(Collectors.toSet());
		//this.plugin.info(String.format("Found %s living entities in chunk", entities.size()));

		entities.forEach(entity -> {
			ImmutableMap<ITrait, TraitType> traits = manager.getAllTraitsOf(entity);
			if(traits.isEmpty()){
				manager.generateTraitsFor(entity);
			}
		});
	}

}
