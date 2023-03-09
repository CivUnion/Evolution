package com.github.longboyy.evolution.util;

import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.traits.ITrait;
import com.github.longboyy.evolution.traits.TraitEntity;
import com.github.longboyy.evolution.traits.TraitManager;
import com.github.longboyy.evolution.traits.TraitType;
import com.github.longboyy.evolution.traits.impl.*;
import com.google.common.collect.ImmutableSet;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.utilities.BiasedRandomPicker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class TraitUtils {

	public static Expression createVariationExpression(String exp){
		return new ExpressionBuilder(exp).variable("x").build();
	}

	public static ItemMap getItemsByItemStack(ItemMap map, ItemStack item){
		ItemMap newMap = new ItemMap();
		List<ItemStack> stacks = map.getItemStackRepresentation();
		for(ItemStack itemStack : stacks){
			if(ItemUtils.areItemsSimilar(itemStack, item)){
				newMap.addItemStack(itemStack);
			}
		}
		return newMap;
	}

	public static ItemMap addItem(ItemMap originalMap, ItemStack itemStack){
		ItemMap newMap = originalMap.clone();

		ItemMap itemMap = newMap.getStacksByMaterial(itemStack);
		List<ItemStack> itemStacks = itemMap.getItemStackRepresentation();

		if(itemStacks.isEmpty()){
			newMap.addItemStack(itemStack);
		}else{
			int totalItems = 0;
			for(ItemStack item : itemStacks){
				totalItems += item.getAmount();
				newMap.removeItemStack(item);
			}

			double stacks = totalItems % 64;

			ItemStack item = itemStack.clone();
			item.setAmount(1);
			ItemMap map = new ItemMap(item);
			map.multiplyContent(stacks);

			newMap.addAll(map.getItemStackRepresentation());
		}
		return newMap;
	}

	public static ImmutableSet<TraitEntity> getEntitiesWithTrait(ITrait trait, TraitType type){
		TraitManager manager = Evolution.getInstance().getTraitManager();
		Set<TraitEntity> entities = new HashSet<>();
		for(World world : Bukkit.getWorlds()){
			for(Entity ent : world.getEntities()){
				if(!(ent instanceof LivingEntity)){
					continue;
				}
				TraitEntity entity = new TraitEntity(ent);
				if(!entity.hasTrait(trait, type)){
					continue;
				}
				entities.add(entity);
			}
		}

		return ImmutableSet.copyOf(entities);
	}

	public static void registerDefaultTraits(TraitManager manager){
		// Husbandry Traits
		manager.registerTrait(new BoneTrait());
		manager.registerTrait(new FertileTrait());
		manager.registerTrait(new LeatherTrait());
		manager.registerTrait(new WoolTrait());

		// Utility Traits
		manager.registerTrait(new HealthTrait());
		manager.registerTrait(new SpeedTrait());
		manager.registerTrait(new InventorySizeTrait());

		// Illness Traits
		manager.registerTrait(new SicklyTrait());
	}

	public static ImmutableSet<ITrait> generateUniqueTraits(ImmutableSet<ITrait> traits, int amount){
		BiasedRandomPicker<ITrait> picker = TraitPickerBuilder.builder(traits).build();

		if(picker == null){
			return ImmutableSet.copyOf(new ITrait[0]);
		}

		int traitsToPick = Math.min(amount, traits.size());
		Set<ITrait> pickedTraits = new HashSet<>();
		while(pickedTraits.size() < traitsToPick){
			ITrait pickedTrait = picker.getRandom();
			if(pickedTraits.contains(pickedTrait)){
				continue;
			}
			pickedTraits.add(pickedTrait);
		}

		return ImmutableSet.copyOf(pickedTraits);
	}

}
