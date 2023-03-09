package com.github.longboyy.evolution.traits.impl;

import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.traits.*;
import com.github.longboyy.evolution.traits.configs.ExpressionTraitConfig;
import com.github.longboyy.evolution.util.TraitUtils;
import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.utilities.cooldowns.TickCoolDownHandler;

import java.util.List;
import java.util.UUID;

public class FertileTrait extends ListenerTrait<FertileTrait.FertileTraitConfig> {

	public static class FertileTraitConfig extends ExpressionTraitConfig {
		protected long minTimeBetweenBreeds = 6000L;
		protected long maxTimeBetweenBreeds = 12000L;
		protected long differenceBetweenBreeds = 6000L;
		protected double sicknessLimit = 0.97D;

		public FertileTraitConfig(){
		}

		@Override
		public void parse(ConfigurationSection section) {
			super.parse(section);
			this.minTimeBetweenBreeds = section.getLong("minTimeBetweenBreeds", 6000L);
			this.maxTimeBetweenBreeds = section.getLong("maxTimeBetweenBreeds", 12000L);
			differenceBetweenBreeds = this.maxTimeBetweenBreeds - this.minTimeBetweenBreeds;
			this.sicknessLimit = section.getDouble("sicknessLimit", 0.97D);
		}
	}

	private final NamespacedKey loveTickKey;
	private final NamespacedKey loveCheckTimeKey;

	private final TickCoolDownHandler<UUID> cooldownHandler;

	//private final long fertileBaseTimeTicks = 6000L;

	//Every 10 minutes.
	private final long fertileCheckTicks = 1200L;
	private int taskId = -1;

	//private long maxValue = 5800L;

	private final TraitManager manager;

	public FertileTrait() {
		super("fertile", TraitCategory.HUSBANDRY, ImmutableSet.copyOf(new EntityType[]{
				EntityType.WOLF,
				EntityType.CAT,
				EntityType.AXOLOTL,
				EntityType.LLAMA,
				EntityType.RABBIT,
				EntityType.TURTLE,
				EntityType.PANDA,
				EntityType.FOX,
				EntityType.BEE,
				EntityType.STRIDER,
				EntityType.HOGLIN,
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
		this.loveCheckTimeKey = new NamespacedKey(Evolution.getInstance(), "fertile_last_check");
		this.cooldownHandler = new TickCoolDownHandler<>(Evolution.getInstance(), 20L);

		this.manager = Evolution.getInstance().getTraitManager();

		this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Evolution.getInstance(), () -> {
			//ImmutableSet<LivingEntity> entities = Evolution.getInstance().getTraitManager().getEntitiesWith(this, TraitType.ACTIVE);
			for(World world : Bukkit.getWorlds()){
				List<TraitEntity> ents = world.getEntities().stream()
						.filter(ent -> this.getAllowedTypes().contains(ent.getType()))
						.map(TraitEntity::new)
						.toList();

				for(TraitEntity entity : ents){
					long lastCheck = this.getLastLoveCheck(entity);
					long ticksPassed = this.fertileCheckTicks;
					if(lastCheck != -1L){
						long currentTime = System.currentTimeMillis();
						ticksPassed = (currentTime - lastCheck) / 50L;
					}

					this.setLastLoveCheck(entity);
					long time = this.getLoveTime(entity);

					if(time > 0) {
						long newTime = Math.max(time - ticksPassed, 0L);
						this.setLoveTime(entity, newTime);
						if(newTime <= 0) {
							Evolution.getInstance().info(String.format("Entity %s(%s[%s]) is now able to be bred again",
									entity.getName(),
									entity.getType(),
									entity.getUniqueId()));
						}
					}
				}
			}
			//ImmutableSet<TraitEntity> entities = TraitUtils.getEntitiesWithTrait(this, TraitType.ACTIVE);
		}, 0L, this.fertileCheckTicks);
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

		Evolution.getInstance().info("Entity breed event - no sickness");

		if(this.getLoveTime(mother) > 0L){
			Evolution.getInstance().info("Entity breed event - mother on cooldown");
			event.setCancelled(true);
			return;
		}


		if(this.getLoveTime(father) > 0L){
			Evolution.getInstance().info("Entity breed event - father on cooldown");
			event.setCancelled(true);
			return;
		}

		if(mother.hasTrait(this, TraitType.ACTIVE)){
			double modifier = this.getModifier(mother);
			//long loveTime = Math.round(this.fertileBaseTimeTicks-(this.maxValue*modifier));
			//long diff = this.config.maxTimeBetweenBreeds - this.config.minTimeBetweenBreeds;
			long loveTime = Math.round(this.config.maxTimeBetweenBreeds - (this.config.differenceBetweenBreeds*modifier));
			this.setLoveTime(mother, loveTime);
			Evolution.getInstance().info("Set mother love time");
		}else{
			this.setLoveTime(mother, this.config.maxTimeBetweenBreeds);
			Evolution.getInstance().info("Entity breed event - set max love time for mother");
		}

		if(father.hasTrait(this, TraitType.ACTIVE)){
			double modifier = this.getModifier(father);
			//long loveTime = Math.round(this.fertileBaseTimeTicks-(this.maxValue*modifier));
			long loveTime = Math.round(this.config.maxTimeBetweenBreeds - (this.config.differenceBetweenBreeds*modifier));
			this.setLoveTime(father, loveTime);
			Evolution.getInstance().info("Set father love time");
		}else{
			this.setLoveTime(father, this.config.maxTimeBetweenBreeds);
			Evolution.getInstance().info("Entity breed event - set max love time for father");
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

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEntityEvent event){
		if(!(event.getRightClicked() instanceof Animals animal)
				|| cooldownHandler.onCoolDown(event.getPlayer().getUniqueId())){
			return;
		}
		cooldownHandler.putOnCoolDown(event.getPlayer().getUniqueId());

		TraitEntity entity = new TraitEntity(event.getRightClicked());
		//LivingEntity entity = (LivingEntity) event.getRightClicked();
		/*
		if(!(entity.entity instanceof Animals)){
			//Evolution.getInstance().info("Attempted to breed entity but it was not an animal");
			return;
		}
		 */

		/*
		if(!entity.hasTrait(this, TraitType.ACTIVE)){
			//Evolution.getInstance().info("Attempted to breed entity but it did not have a valid trait");
			return;
		}
		 */

		long time = this.getLoveTime(entity);

		if(time > 0L){
			//Evolution.getInstance().info("Attempted to breed entity but it was not ready to breed yet");
			event.setCancelled(true);
			return;
		}

		//Animals animal = (Animals) entity.entity;
		if(!animal.isAdult()){
			return;
		}

		Player player = event.getPlayer();
		ItemStack item = event.getHand() == EquipmentSlot.HAND
				? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();

		if(!animal.isBreedItem(item)){
			//Evolution.getInstance().info("Attempted to breed entity but player was not holding correct item");
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
			//this.setLoveTime(entity, Math.round(this.fertileBaseTimeTicks-(this.maxValue*this.getModifier(entity))));
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
	public TextComponent.Builder displayInfo(TraitEntity entity) {
		TextComponent.Builder builder = super.displayInfo(entity);
		builder.append(Component.newline());
		builder.append(Component.text("Next breed:"));
		builder.append(Component.space());
		float time = this.getLoveTime(entity)/20f;
		builder.append(Component.text(time > 0 ? time : 0));
		builder.append(Component.space());
		builder.append(Component.text("seconds"));
		return builder;
	}

	@Override
	public String getPrettyName(TraitEntity entity) {
		return entity.getVariation(this) >= 0 ? "Fertile" : "Infertile";
	}

	@Override
	protected Class<FertileTraitConfig> getConfigClass() {
		return FertileTraitConfig.class;
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

	private long getLastLoveCheck(TraitEntity entity){
		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		if(pdc.has(this.loveCheckTimeKey)){
			return pdc.get(this.loveCheckTimeKey, PersistentDataType.LONG);
		}
		return -1L;
	}

	private void setLastLoveCheck(TraitEntity entity){
		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		pdc.set(this.loveCheckTimeKey, PersistentDataType.LONG, System.currentTimeMillis());
	}

	private double getModifier(TraitEntity entity){
		double variation = this.getVariation(entity);
		Expression exp = (variation >= 0) ? this.config.getNegativeExpression() : this.config.getPositiveExpression();
		return exp.setVariable("x", (variation >= 0 ? -variation : variation)).evaluate();
	}
}
