package com.github.longboyy.evolution.listeners;

import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.traits.ITrait;
import com.github.longboyy.evolution.traits.TraitEntity;
import com.github.longboyy.evolution.traits.TraitType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class PlayerListener implements Listener {

	private final Evolution plugin;

	public PlayerListener(Evolution plugin){
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerInteractWithEntity(PlayerInteractEntityEvent event){
		if(!(event.getRightClicked() instanceof LivingEntity)){
			return;
		}

		Player player = event.getPlayer();
		ItemStack item = event.getHand() == EquipmentSlot.HAND
				? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();

		if(item.getType() != Material.CLOCK){
			return;
		}

		TraitEntity entity = new TraitEntity(event.getRightClicked());
		//LivingEntity entity = (LivingEntity)event.getRightClicked();
		ImmutableMap<ITrait, TraitType> traits = entity.getTraits();

		if(traits == null || traits.size() == 0){
			this.plugin.info(String.format("Failed to find any traits on entity type '%s' with UUID '%s'", entity.entity.getType(), entity.entity.getUniqueId()));
			return;
		}

		TextComponent.Builder msg = Component.text();
		msg.append(Component.newline());
		msg.append(Component.text(String.format("%s's", entity.entity.getName()), TextColor.color(230, 140, 50), TextDecoration.UNDERLINED));
		msg.append(Component.space().decorate(TextDecoration.UNDERLINED).color(TextColor.color(230, 140, 50)));
		msg.append(Component.text("traits:").decorate(TextDecoration.UNDERLINED).color(TextColor.color(230, 140, 50)));
		msg.append(Component.newline());
		msg.append(Component.newline());

		ImmutableSet<ITrait> activeTraits = entity.getTraits(TraitType.ACTIVE);
		if(activeTraits != null){
			msg.append(Component.text("Active", Evolution.SUCCESS_GREEN, TextDecoration.UNDERLINED));
			msg.append(Component.newline());
			activeTraits.forEach(trait -> this.generateText(trait, entity, msg));
			msg.append(Component.newline());
			//activeTraits.forEach(trait);
		}
		ImmutableSet<ITrait> inactiveTraits = entity.getTraits(TraitType.INACTIVE);
		if(inactiveTraits != null){
			msg.append(Component.text("Inactive", Evolution.FAILURE_RED, TextDecoration.UNDERLINED));
			msg.append(Component.newline());
			inactiveTraits.forEach(trait -> this.generateText(trait, entity, msg));
		}

		/*
		for(Map.Entry<ITrait, TraitType> traitEntry : traits.entrySet()){
			ITrait trait = traitEntry.getKey();
			TraitType type = traitEntry.getValue();

			//msg.append(Component.newline());

			TextComponent.Builder traitBuilder = Component.text();
			traitBuilder.append(Component.text(trait.getPrettyName(), type == TraitType.ACTIVE ? Evolution.SUCCESS_GREEN : Evolution.FAILURE_RED, TextDecoration.UNDERLINED));
			//traitBuilder.append(Component.newline());
			msg.append(traitBuilder.hoverEvent(HoverEvent.showText(trait.displayInfo(entity).build())));
			//msg.append(trait.displayInfo(entity, traitBuilder).build());
			msg.append(Component.newline());
		}
		 */

		player.sendMessage(msg.build());
		event.setCancelled(true);
	}

	private void generateText(ITrait trait, TraitEntity entity, TextComponent.Builder builder){
		TextComponent.Builder traitBuilder = Component.text();
		traitBuilder.append(Component.text(trait.getPrettyName()));
		builder.append(traitBuilder.hoverEvent(HoverEvent.showText(trait.displayInfo(entity).build())));
		builder.append(Component.newline());
	}

}
