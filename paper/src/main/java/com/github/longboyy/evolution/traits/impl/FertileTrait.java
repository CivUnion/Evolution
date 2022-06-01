package com.github.longboyy.evolution.traits.impl;

import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.traits.*;
import com.github.longboyy.evolution.util.TraitUtils;
import com.google.common.collect.ImmutableSet;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.utilities.cooldowns.TickCoolDownHandler;

import java.util.UUID;

public class FertileTrait extends ListenerTrait {

	private static final String DEFAULT_NEGATIVE_EXPRESSION = "-(log(1+x)/log(2))^0.7";
	private static final String DEFAULT_POSITIVE_EXPRESSION = "(log(1-x)/log(2))^0.7";

	private final NamespacedKey loveTickKey;
	private final TraitManager manager;

	private final TickCoolDownHandler<UUID> cooldownHandler;

	private long fertileBaseTimeTicks = 6000L;
	private long fertileCheckTicks = 20L;
	private int taskId = -1;

	private long maxValue = 5800L;
	private Expression positiveExpression = this.createExpression(DEFAULT_POSITIVE_EXPRESSION);
	private Expression negativeExpression = this.createExpression(DEFAULT_NEGATIVE_EXPRESSION);
	private double maxVariationPerGeneration = 0.005D;

	public FertileTrait() {
		super("fertile", TraitCategory.HUSBANDRY, ImmutableSet.copyOf(new EntityType[]{
				EntityType.COW,
				EntityType.MUSHROOM_COW,
				EntityType.PIG,
				EntityType.SHEEP,
				EntityType.HORSE,
				EntityType.MULE,
				EntityType.DONKEY,
				EntityType.CHICKEN,
				EntityType.GOAT
		}));

		this.loveTickKey = new NamespacedKey(Evolution.getInstance(), "fertile_love_ticks");
		this.cooldownHandler = new TickCoolDownHandler<>(Evolution.getInstance(), 20L);

		this.manager = Evolution.getInstance().getTraitManager();
		//TraitListener listener = this.manager.getListener();

		/*
		listener.registerEvent(EntityBreedEvent.class, _event -> {
			EntityBreedEvent event = (EntityBreedEvent) _event;

			TraitEntity mother = new TraitEntity(event.getMother());
			TraitEntity father = new TraitEntity(event.getFather());

			Evolution.getInstance().info("Entity breed event");

			if(mother.hasTrait(this, TraitType.ACTIVE)){
				if(this.getLoveTime(mother) > 0L){
					event.setCancelled(true);
					return;
				}

				double modifier = this.getModifier(mother);
				long loveTime = Math.round(this.fertileBaseTimeTicks-(this.maxValue*modifier));
				this.setLoveTime(mother, loveTime);
				Evolution.getInstance().info("Set mother love time");
			}

			if(father.hasTrait(this, TraitType.ACTIVE)){
				if(this.getLoveTime(father) > 0L){
					event.setCancelled(true);
					return;
				}

				double modifier = this.getModifier(father);
				long loveTime = Math.round(this.fertileBaseTimeTicks-(this.maxValue*modifier));
				this.setLoveTime(father, loveTime);
				Evolution.getInstance().info("Set father love time");
			}
		});

		listener.registerEvent(EntityEnterLoveModeEvent.class, _event -> {
			EntityEnterLoveModeEvent event = (EntityEnterLoveModeEvent) _event;

			TraitEntity entity = new TraitEntity(event.getEntity());

			if(event.isCancelled() || !this.hasLoveTime(entity)){
				return;
			}

			if(this.getLoveTime(entity) > 0L){
				event.setCancelled(true);
			}
		});

		listener.registerEvent(PlayerInteractEntityEvent.class, _event -> {
			PlayerInteractEntityEvent event = (PlayerInteractEntityEvent) _event;

			if(event.isCancelled()
					|| !(event.getRightClicked() instanceof LivingEntity)
					|| cooldownHandler.onCoolDown(event.getPlayer().getUniqueId())){
				return;
			}
			cooldownHandler.putOnCoolDown(event.getPlayer().getUniqueId());

			TraitEntity entity = new TraitEntity(event.getRightClicked());
			//LivingEntity entity = (LivingEntity) event.getRightClicked();
			if(!(entity.entity instanceof Animals)){
				Evolution.getInstance().info("Attempted to breed entity but it was not an animal");
				return;
			}

			if(!entity.hasTrait(this, TraitType.ACTIVE)){
				Evolution.getInstance().info("Attempted to breed entity but it did not have a valid trait");
				return;
			}

			long time = this.getLoveTime(entity);

			if(time > 0L){
				Evolution.getInstance().info("Attempted to breed entity but it was not ready to breed yet");
				event.setCancelled(true);
				return;
			}

			Animals animal = (Animals) entity.entity;
			if(!animal.isAdult()){
				return;
			}

			Player player = event.getPlayer();
			ItemStack item = event.getHand() == EquipmentSlot.HAND
					? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();

			if(!animal.isBreedItem(item)){
				Evolution.getInstance().info("Attempted to breed entity but player was not holding correct item");
				return;
			}

			ItemStack breedItem = item.clone();
			breedItem.setAmount(1);

			ItemMap breedItemMap = new ItemMap(breedItem);
			if(breedItemMap.removeSafelyFrom(player.getInventory())) {
				animal.setBreedCause(player.getUniqueId());
				animal.setLoveModeTicks(600);
				animal.setBreed(true);
				animal.getWorld().spawnParticle(Particle.HEART, animal.getLocation().toCenterLocation().add(0D, animal.getHeight(), 0D), 1);
				Evolution.getInstance().info("We should be in love mode!");
				this.setLoveTime(entity, Math.round(this.fertileBaseTimeTicks-(this.maxValue*this.getModifier(entity))));
			}
		});
		*/

		this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Evolution.getInstance(), () -> {
			//ImmutableSet<LivingEntity> entities = Evolution.getInstance().getTraitManager().getEntitiesWith(this, TraitType.ACTIVE);
			ImmutableSet<TraitEntity> entities = TraitUtils.getEntitiesWithTrait(this, TraitType.ACTIVE);
			for(TraitEntity entity : entities){
				long time = this.getLoveTime(entity);

				if(time > 0) {
					long newTime = Math.max(time - this.fertileCheckTicks, 0L);
					this.setLoveTime(entity, newTime);
					if(newTime <= 0) {
						Evolution.getInstance().info(String.format("Entity %s(%s[%s]) is now able to be bred again",
								entity.getName(),
								entity.getType(),
								entity.getUniqueId()));
					}
				}
			}
		}, 0L, this.fertileCheckTicks);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityBreed(EntityBreedEvent event){
		TraitEntity mother = new TraitEntity(event.getMother());
		TraitEntity father = new TraitEntity(event.getFather());

		Evolution.getInstance().info("Entity breed event");

		if(mother.hasTrait(this, TraitType.ACTIVE)){
			if(this.getLoveTime(mother) > 0L){
				event.setCancelled(true);
				return;
			}

			double modifier = this.getModifier(mother);
			long loveTime = Math.round(this.fertileBaseTimeTicks-(this.maxValue*modifier));
			this.setLoveTime(mother, loveTime);
			Evolution.getInstance().info("Set mother love time");
		}

		if(father.hasTrait(this, TraitType.ACTIVE)){
			if(this.getLoveTime(father) > 0L){
				event.setCancelled(true);
				return;
			}

			double modifier = this.getModifier(father);
			long loveTime = Math.round(this.fertileBaseTimeTicks-(this.maxValue*modifier));
			this.setLoveTime(father, loveTime);
			Evolution.getInstance().info("Set father love time");
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityEnterLoveMode(EntityEnterLoveModeEvent event){
		TraitEntity entity = new TraitEntity(event.getEntity());

		if(event.isCancelled() || !this.hasLoveTime(entity)){
			return;
		}

		if(this.getLoveTime(entity) > 0L){
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEntityEvent event){
		if(event.isCancelled()
				|| !(event.getRightClicked() instanceof LivingEntity)
				|| cooldownHandler.onCoolDown(event.getPlayer().getUniqueId())){
			return;
		}
		cooldownHandler.putOnCoolDown(event.getPlayer().getUniqueId());

		TraitEntity entity = new TraitEntity(event.getRightClicked());
		//LivingEntity entity = (LivingEntity) event.getRightClicked();
		if(!(entity.entity instanceof Animals)){
			Evolution.getInstance().info("Attempted to breed entity but it was not an animal");
			return;
		}

		if(!entity.hasTrait(this, TraitType.ACTIVE)){
			Evolution.getInstance().info("Attempted to breed entity but it did not have a valid trait");
			return;
		}

		long time = this.getLoveTime(entity);

		if(time > 0L){
			Evolution.getInstance().info("Attempted to breed entity but it was not ready to breed yet");
			event.setCancelled(true);
			return;
		}

		Animals animal = (Animals) entity.entity;
		if(!animal.isAdult()){
			return;
		}

		Player player = event.getPlayer();
		ItemStack item = event.getHand() == EquipmentSlot.HAND
				? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();

		if(!animal.isBreedItem(item)){
			Evolution.getInstance().info("Attempted to breed entity but player was not holding correct item");
			return;
		}

		ItemStack breedItem = item.clone();
		breedItem.setAmount(1);

		ItemMap breedItemMap = new ItemMap(breedItem);
		if(breedItemMap.removeSafelyFrom(player.getInventory())) {
			animal.setBreedCause(player.getUniqueId());
			animal.setLoveModeTicks(600);
			animal.setBreed(true);
			animal.getWorld().spawnParticle(Particle.HEART, animal.getLocation().toCenterLocation().add(0D, animal.getHeight(), 0D), 1);
			Evolution.getInstance().info("We should be in love mode!");
			this.setLoveTime(entity, Math.round(this.fertileBaseTimeTicks-(this.maxValue*this.getModifier(entity))));
		}
	}

	@Override
	public boolean applyTrait(TraitEntity entity, double variation) {
		boolean success = super.applyTrait(entity, variation);
		if(success){
			this.setLoveTime(entity, 0L);
		}
		return success;
	}

	@Override
	public String getPrettyName(TraitEntity entity) {
		return "Fertile";
	}

	@Override
	public double getMaxVariation() {
		return this.maxVariationPerGeneration;
	}

	@Override
	public void parseConfig(ConfigurationSection section) {
		super.parseConfig(section);
		if(section != null){
			if(section.isConfigurationSection("expression")){
				ConfigurationSection expSection = section.getConfigurationSection("expression");
				this.negativeExpression = this.createExpression(expSection.getString("negative", DEFAULT_NEGATIVE_EXPRESSION));
				this.positiveExpression = this.createExpression(expSection.getString("positive", DEFAULT_POSITIVE_EXPRESSION));
			}else if(section.isString("expression")){
				String exp = section.getString("expression", DEFAULT_POSITIVE_EXPRESSION);
				this.negativeExpression = this.createExpression(exp);
				this.positiveExpression = this.createExpression(exp);
			}
		}
	}

	@Override
	public double getWeight(TraitEntity entity) {
		return 0.5D;
	}

	private Expression createExpression(String exp){
		return new ExpressionBuilder(exp).variable("x").build();
	}

	private void setLoveTime(TraitEntity entity, long timeTicks){
		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		pdc.set(this.loveTickKey, PersistentDataType.LONG, timeTicks);
	}

	private boolean hasLoveTime(TraitEntity entity){
		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		return pdc.has(this.loveTickKey);
	}

	private long getLoveTime(TraitEntity entity){
		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		if(pdc.has(this.loveTickKey)){
			return pdc.get(this.loveTickKey, PersistentDataType.LONG);
		}
		return -1L;
	}

	private double getModifier(TraitEntity entity){
		double variation = this.getVariation(entity);
		Expression exp = (variation >= 0) ? this.positiveExpression : this.negativeExpression;
		return exp.setVariable("x", this.getVariation(entity)).evaluate();
	}
}
