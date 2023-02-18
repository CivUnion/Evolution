package com.github.longboyy.evolution.traits.impl;

import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.traits.*;
import com.github.longboyy.evolution.traits.configs.ExpressionTraitConfig;
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

public class FertileTrait extends ListenerTrait<FertileTrait.FertileTraitConfig> {

	public static class FertileTraitConfig extends ExpressionTraitConfig {

		protected long maxAdditionalTime = 5800L;
		protected long baseTimeBetweenBreeds = 6000L;
		protected double sicknessLimit = 0.97D;

		public FertileTraitConfig(){
		}

		@Override
		public void parse(ConfigurationSection section) {
			super.parse(section);
			this.baseTimeBetweenBreeds = section.getLong("baseTimeBetweenBreeds", 6000L);
			this.sicknessLimit = section.getDouble("sicknessLimit", 0.97D);
		}
	}

	private final NamespacedKey loveTickKey;

	private final TickCoolDownHandler<UUID> cooldownHandler;

	private final long fertileBaseTimeTicks = 6000L;
	private final long fertileCheckTicks = 20L;
	private int taskId = -1;

	private long maxValue = 5800L;

	private final TraitManager manager;

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

	@Override
	public double getMaxVariation() {
		return 0.005;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityBreed(EntityBreedEvent event){
		SicklyTrait sicklyTrait = this.manager.getTrait(SicklyTrait.class);
		TraitEntity mother = new TraitEntity(event.getMother());
		TraitEntity father = new TraitEntity(event.getFather());

		Evolution.getInstance().info("Entity breed event");

		if(mother.getVariation(sicklyTrait) >= this.config.sicknessLimit || father.getVariation(sicklyTrait) >= this.config.sicknessLimit) {
			event.setCancelled(true);
			return;
		}

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
		return entity.getVariation(this) >= 0 ? "Fertile" : "Impotent";
	}

	@Override
	protected Class<FertileTraitConfig> getConfigClass() {
		return FertileTraitConfig.class;
	}

	@Override
	public double getWeight(TraitEntity entity) {
		return 0.5D;
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
		Expression exp = (variation >= 0) ? this.config.getNegativeExpression() : this.config.getPositiveExpression();
		return exp.setVariable("x", (variation >= 0 ? -variation : variation)).evaluate();
	}
}
