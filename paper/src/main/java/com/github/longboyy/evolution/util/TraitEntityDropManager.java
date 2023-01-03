package com.github.longboyy.evolution.util;

import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.traits.ITrait;
import com.github.longboyy.evolution.traits.TraitEntity;
import com.github.longboyy.evolution.traits.TraitManager;
import com.github.longboyy.evolution.traits.TraitType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

import java.util.*;
import java.util.function.BiConsumer;

public class TraitEntityDropManager implements Listener {

	private static TraitEntityDropManager instance = null;

	public static TraitEntityDropManager getInstance(){
		return instance;
	}

	protected Map<ITrait, BiConsumer<TraitEntity, ItemMap>> dropOverrides = new HashMap<>();

	public TraitEntityDropManager(){
		if(instance == null) {
			instance = this;
		}
	}

	public void registerDrop(ITrait trait, BiConsumer<TraitEntity, ItemMap> dropFunction){
		dropOverrides.put(Objects.requireNonNull(trait), Objects.requireNonNull(dropFunction));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleDeath(EntityDeathEvent event){
		TraitEntity entity = new TraitEntity(event.getEntity());
		ItemMap itemMap = new ItemMap(event.getDrops());
		Evolution.getInstance().getTraitManager().getTraits(entity.getType()).forEach(trait -> {
			if(dropOverrides.containsKey(trait)){
				BiConsumer<TraitEntity, ItemMap> dropFunction = dropOverrides.get(trait);

				if(dropFunction != null){
					dropFunction.accept(entity, itemMap);
				}

			}
		});

		event.getDrops().clear();
		event.getDrops().addAll(itemMap.getItemStackRepresentation());
	}
}
