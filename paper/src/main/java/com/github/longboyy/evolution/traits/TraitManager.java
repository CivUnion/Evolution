package com.github.longboyy.evolution.traits;

import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.util.TraitUtils;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.*;

public class TraitManager {

	//private final Map<TraitCategory, Set<ITrait>> traitByCategory;

	private final Evolution plugin;

	private final Set<ITrait> traits;
	private final Map<TraitCategory, Set<ITrait>> traitsByCategory;
	private final Map<Class<? extends ITrait>, ITrait> traitsByClass;
	private final Map<String, ITrait> traitById;
	private final Map<EntityType, Set<ITrait>> traitsByEntityType;

	private ConfigurationSection traitSection = null;

	public TraitManager(Evolution plugin){
		this.plugin = plugin;
		this.traits = new HashSet<>();
		this.traitsByCategory = new HashMap<>();
		this.traitsByClass = new HashMap<>();
		this.traitById = new HashMap<>();
		this.traitsByEntityType = new HashMap<>();
		PlayerPortalEvent p;
	}

	public void parseConfig(ConfigurationSection section){
		if(section != null){
			this.traitSection = section;
		}

		this.clearTraits();
		TraitUtils.registerDefaultTraits(this);
	}

	public void registerTrait(ITrait trait){
		if(trait == null){
			return;
		}

		if(this.traits.contains(trait)){
			return;
		}

		this.plugin.info(String.format("Registering Trait '%s'", trait.getIdentifier()));

		trait.parseConfig(this.traitSection != null ? this.traitSection.getConfigurationSection(trait.getIdentifier()) : null);

		if(!trait.isEnabled()){
			return;
		}

		this.traits.add(trait);
		this.traitById.put(trait.getIdentifier(), trait);
		this.traitsByClass.put(trait.getClass(), trait);

		if(!this.traitsByCategory.containsKey(trait.getCategory())){
			this.traitsByCategory.put(trait.getCategory(), new HashSet<>());
		}
		Set<ITrait> traitSet = this.traitsByCategory.get(trait.getCategory());
		traitSet.add(trait);

		ImmutableSet<EntityType> types = trait.getAllowedTypes();

		for(EntityType type : types){
			if(!this.traitsByEntityType.containsKey(type)){
				this.traitsByEntityType.put(type, new HashSet<>());
			}
			Set<ITrait> typeTraits = this.traitsByEntityType.get(type);
			typeTraits.add(trait);
		}

		if(trait instanceof ListenerTrait){
			this.plugin.registerListener((ListenerTrait)trait);
		}

		this.plugin.info(String.format("Registered Trait '%s'", trait.getIdentifier()));

	}

	public ITrait getTrait(String id){
		return this.traitById.get(id);
	}

	public <T extends ITrait> T getTrait(Class<? extends T> clazz){
		ITrait trait = this.traitsByClass.get(clazz);
		try{
			return (T)trait;
		}catch(Exception e){
			return null;
		}
	}

	public ImmutableSet<ITrait> getTraits(EntityType type){
		Set<ITrait> traits = this.traitsByEntityType.get(type);
		if(traits == null){
			return ImmutableSet.copyOf(new HashSet<>());
		}
		return ImmutableSet.copyOf(traits);
	}

	public ImmutableSet<ITrait> getTraits(TraitCategory category){
		return ImmutableSet.copyOf(this.traitsByCategory.get(category));
	}

	public ImmutableSet<ITrait> getTraits(){
		return ImmutableSet.copyOf(this.traits);
	}

	private void clearTraits(){
		this.traits.forEach(trait -> {
			if(trait instanceof ListenerTrait){
				HandlerList.unregisterAll((ListenerTrait) trait);
			}
		});

		this.traits.clear();
		this.traitById.clear();
		this.traitsByClass.clear();
		this.traitsByCategory.clear();
		this.traitsByEntityType.clear();
	}

	/*
	public ImmutableSet<LivingEntity> getEntitiesWith(ITrait trait){
		Set<LivingEntity> resultSet = new HashSet<>();
		for(World world : Bukkit.getWorlds()){
			for(LivingEntity entity : world.getLivingEntities()){
				if(this.hasTrait(entity, trait)){
					resultSet.add(entity);
				}
			}
		}
		return ImmutableSet.copyOf(resultSet);
	}

	public ImmutableSet<LivingEntity> getEntitiesWith(ITrait trait, TraitType type){
		Set<LivingEntity> resultSet = new HashSet<>();
		for(World world : Bukkit.getWorlds()){
			for(LivingEntity entity : world.getLivingEntities()){
				if(this.hasTrait(entity, trait, type)){
					resultSet.add(entity);
				}
			}
		}
		return ImmutableSet.copyOf(resultSet);
	}

	public boolean hasTrait(LivingEntity entity, ITrait trait, TraitType type){
		if(entity == null || trait == null || type == null){
			return false;
		}

		ImmutableSet<ITrait> traits = type.getTraitsOf(entity);
		return (traits != null && traits.contains(trait));
	}

	public boolean hasTrait(LivingEntity entity, ITrait trait){
		return this.hasTrait(entity, trait, TraitType.ACTIVE) || this.hasTrait(entity, trait, TraitType.INACTIVE);
	}

	public ImmutableSet<ITrait> getActiveTraitsOf(LivingEntity entity){
		return this.getEntityTraits(TraitType.ACTIVE, entity);
	}

	public void setActiveTraitsOf(LivingEntity entity, ImmutableSet<ITrait> traits){
		if(entity.getType() == EntityType.PLAYER){
			return;
		}

		if(!this.setEntityTraits(TraitType.ACTIVE, entity, traits)){
			this.plugin.info(String.format("Failed to set active traits of entity type '%s' with UUID '%s'", entity.getType(), entity.getUniqueId()));
		}
	}

	public ImmutableSet<ITrait> getInactiveTraitsOf(LivingEntity entity){
		return this.getEntityTraits(TraitType.INACTIVE, entity);
	}

	public void setInactiveTraitsOf(LivingEntity entity, ImmutableSet<ITrait> traits){
		if(entity.getType() == EntityType.PLAYER){
			return;
		}

		this.setEntityTraits(TraitType.INACTIVE, entity, traits);
	}

	public ImmutableMap<ITrait, TraitType> getAllTraitsOf(LivingEntity entity){

		//ImmutableSet<ITrait> inactiveTraits = this.getInactiveTraitsOf(entity);
		//ImmutableSet<ITrait> activeTraits = this.getActiveTraitsOf(entity);
		ImmutableSet<ITrait> inactiveTraits = TraitType.INACTIVE.getTraitsOf(entity);
		ImmutableSet<ITrait> activeTraits = TraitType.ACTIVE.getTraitsOf(entity);

		if(inactiveTraits == null || inactiveTraits.isEmpty()){
			inactiveTraits = ImmutableSet.copyOf(new ITrait[0]);
		}

		if(activeTraits == null || activeTraits.isEmpty()){
			activeTraits = ImmutableSet.copyOf(new ITrait[0]);
		}

		Map<ITrait, TraitType> allTraits = new HashMap<>();
		if(inactiveTraits != null){
			inactiveTraits.forEach(trait -> allTraits.put(trait, TraitType.INACTIVE));
		}
		if(activeTraits != null){
			activeTraits.forEach(trait -> allTraits.put(trait, TraitType.ACTIVE));
		}
		return ImmutableMap.copyOf(allTraits);
	}

	public void generateTraitsFor(LivingEntity entity, LivingEntity mother, LivingEntity father){
		ImmutableMap<ITrait, TraitType> motherTraits = this.getAllTraitsOf(mother);
		if(motherTraits == null){
			this.generateTraitsFor(mother);
			motherTraits = this.getAllTraitsOf(mother);
		}

		ImmutableMap<ITrait, TraitType> fatherTraits = this.getAllTraitsOf(father);
		if(fatherTraits == null){
			this.generateTraitsFor(father);
			fatherTraits = this.getAllTraitsOf(father);
		}

		if(motherTraits == null || fatherTraits == null || motherTraits.isEmpty() || fatherTraits.isEmpty()){
			this.generateTraitsFor(entity);
			return;
		}
		
		//this.plugin.info()

		Set<ITrait> fullPool = new HashSet<>();
		fullPool.addAll(motherTraits.keySet());
		fullPool.addAll(fatherTraits.keySet());
		//fullPool.removeIf(trait -> fullPool.contains(trait));

		//we need to decide if there is any genetic mutation here
		double mutateChance = this.getConfigOption("breedMutateChance", 0.5D);
		if(Math.random() <= mutateChance){
			Set<ITrait> mutateTraits = new HashSet<>(this.getTraits());
			mutateTraits.removeIf(trait -> fullPool.contains(trait));
			BiasedRandomPicker<ITrait> mutatePicker = this.getRandomPicker(ImmutableSet.copyOf(mutateTraits));
			if(mutatePicker != null) {
				ITrait mutateTrait = mutatePicker.getRandom();
				if (mutateTrait != null) {
					fullPool.add(mutateTrait);
				}
			}
		}

		Set<ITrait> husbandryTraits = new HashSet<>(fullPool);
		husbandryTraits.removeIf(trait -> trait.getCategory() != TraitCategory.HUSBANDRY);
		BiasedRandomPicker<ITrait> husbandryPicker = null;
		if(!husbandryTraits.isEmpty()) {
			Map<ITrait, Double> chances = this.generateChancesWith(ImmutableSet.copyOf(husbandryTraits), motherTraits, fatherTraits);
			husbandryPicker = new BiasedRandomPicker<>(chances);
		}

		Set<ITrait> utilityTraits = new HashSet<>(fullPool);
		utilityTraits.removeIf(trait -> trait.getCategory() != TraitCategory.UTILITY);
		BiasedRandomPicker<ITrait> utilityPicker = null;
		if(!utilityTraits.isEmpty()) {
			Map<ITrait, Double> chances = this.generateChancesWith(ImmutableSet.copyOf(utilityTraits), motherTraits, fatherTraits);
			utilityPicker = new BiasedRandomPicker<>(chances);
		}

		Set<ITrait> illnessTraits = new HashSet<>(fullPool);
		illnessTraits.removeIf(trait -> trait.getCategory() != TraitCategory.ILLNESS);
		BiasedRandomPicker<ITrait> illnessPicker = null;
		if(!illnessTraits.isEmpty()) {
			Map<ITrait, Double> chances = this.generateChancesWith(ImmutableSet.copyOf(illnessTraits), motherTraits, fatherTraits);
			illnessPicker = new BiasedRandomPicker<>(chances);
		}

		Set<ITrait> activeTraits = new HashSet<>();
		Set<ITrait> inactiveTraits = new HashSet<>();

		if(husbandryPicker != null) {
			ITrait activeHusbandry = husbandryPicker.getRandom();
			if(activeHusbandry != null) {
				activeTraits.add(activeHusbandry);
				husbandryTraits.remove(activeHusbandry);
			}
		}

		if(utilityPicker != null) {
			ITrait activeUtility = utilityPicker.getRandom();
			if(activeUtility != null) {
				activeTraits.add(activeUtility);
				utilityTraits.remove(activeUtility);
			}
		}

		if(illnessPicker != null) {
			ITrait activeIllness = illnessPicker.getRandom();
			if(activeIllness != null) {
				activeTraits.add(activeIllness);
				illnessTraits.remove(activeIllness);
			}
		}

		if(!utilityTraits.isEmpty()) {
			inactiveTraits.addAll(TraitUtils.generateUniqueTraits(ImmutableSet.copyOf(utilityTraits), 3));
		}

		if(!husbandryTraits.isEmpty()) {
			inactiveTraits.addAll(TraitUtils.generateUniqueTraits(ImmutableSet.copyOf(husbandryTraits), 3));
		}
		// inherit all illnesses
		inactiveTraits.addAll(illnessTraits);

		this.setActiveTraitsOf(entity, ImmutableSet.copyOf(activeTraits));
		this.setInactiveTraitsOf(entity, ImmutableSet.copyOf(inactiveTraits));

		for(ITrait trait : activeTraits){
			double variation = (trait.getVariation(mother)+trait.getVariation(father))*0.5D;
			variation += trait.getVariation(entity);

			trait.applyTrait(entity, MoreMath.clamp(variation, -1D, 1D));
		}

		//need to do something here with the babies traits
	}

	private Map<ITrait, Double> generateChancesWith(ImmutableSet<ITrait> fullPool, ImmutableMap<ITrait, TraitType> motherTraits, ImmutableMap<ITrait, TraitType> fatherTraits){
		Map<ITrait, Double> chances = new HashMap<>();
		double totalChances = 0D;
		for(ITrait trait : fullPool){
			if(chances.containsKey(trait)){
				continue;
			}

			double chance = 0D;
			if(motherTraits.containsKey(trait) && fatherTraits.containsKey(trait)){
				TraitType motherType = motherTraits.get(trait);
				TraitType fatherType = fatherTraits.get(trait);

				//double chance
				if(motherType == TraitType.ACTIVE && fatherType == TraitType.ACTIVE){
					chance = 3D/4D;
				}else if((motherType == TraitType.ACTIVE && fatherType == TraitType.INACTIVE) || (motherType == TraitType.INACTIVE && fatherType == TraitType.ACTIVE)){
					chance = 1D/2D;
				}else{
					chance = 1D/4D;
				}
			}else{
				if(motherTraits.containsKey(trait)){
					TraitType motherType = motherTraits.get(trait);
					if(motherType == TraitType.ACTIVE){
						chance = 1D/3D;
					}else{
						chance = 1D/8D;
					}
				}else{
					TraitType fatherType = fatherTraits.get(trait);
					if(fatherType == TraitType.ACTIVE){
						chance = 1D/3D;
					}else{
						chance = 1D/8D;

					}
				}
			}

			double finalChance = trait.getWeight()*chance;
			chances.put(trait, finalChance);
			totalChances += finalChance;
		}

		Map<ITrait, Double> realChances = new HashMap<>();
		for(Map.Entry<ITrait, Double> traitChance : chances.entrySet()){
			double realChance = traitChance.getValue()/totalChances;
			realChances.put(traitChance.getKey(), realChance);
		}

		return realChances;
	}

	public void generateTraitsFor(LivingEntity entity){


		ImmutableSet<ITrait> validTraits = this.getTraitsByEntityType(entity.getType());
		if(validTraits.isEmpty()){
			//this.plugin.info(String.format("Attempted to generate traits for entity type '%s' with UUID '%s', but failed to find any valid traits", entity.getType(), entity.getUniqueId()));
			return;
		}

		Set<ITrait> husbandryTraits = validTraits.stream().filter(trait -> trait.getCategory() == TraitCategory.HUSBANDRY).collect(Collectors.toSet());
		Set<ITrait> utilityTraits = validTraits.stream().filter(trait -> trait.getCategory() == TraitCategory.UTILITY).collect(Collectors.toSet());

		ImmutableSet<ITrait> husbandry = TraitUtils.generateUniqueTraits(ImmutableSet.copyOf(husbandryTraits), 3);
		ImmutableSet<ITrait> utility = TraitUtils.generateUniqueTraits(ImmutableSet.copyOf(utilityTraits), 3);
		//need to add illness in here but it should be a random chance to apply.

		Set<ITrait> activeTraits = new HashSet<>();
		Set<ITrait> inactiveTraits = new HashSet<>();

		BiasedRandomPicker<ITrait> activePicker = this.getRandomPicker(husbandry);
		if(activePicker != null) {
			activeTraits.add(activePicker.getRandom());
		}
		activePicker = this.getRandomPicker(utility);
		if(activePicker != null) {
			activeTraits.add(activePicker.getRandom());
		}
		husbandry.forEach(trait -> {
			if(!activeTraits.contains(trait)){
				inactiveTraits.add(trait);
			}
		});

		utility.forEach(trait -> {
			if(!activeTraits.contains(trait)){
				inactiveTraits.add(trait);
			}
		});

		this.setActiveTraitsOf(entity, ImmutableSet.copyOf(activeTraits));
		this.setInactiveTraitsOf(entity, ImmutableSet.copyOf(inactiveTraits));

		for(ITrait trait : activeTraits){
			trait.applyTrait(entity, MoreMath.clamp(trait.getVariation(entity), -1D, 1D));
		}

		for(ITrait trait : inactiveTraits){
			trait.applyTrait(entity, MoreMath.clamp(trait.getVariation(entity), -1D, 1D));
		}

		this.plugin.info(String.format("Generated traits %s inactive traits, and %s active traits for '%s'", inactiveTraits.size(), activeTraits.size(), entity.getUniqueId()));
	}

	public BiasedRandomPicker<ITrait> getRandomPicker(ImmutableSet<ITrait> traits){
		if(traits.isEmpty()){
			return null;
		}

		Map<ITrait, Double> traitChances = new HashMap<>();
		double totalWeights = traits.stream().map(trait -> trait.getWeight()).mapToDouble(val -> val.doubleValue()).sum();

		for(ITrait trait : traits){
			double chance = trait.getWeight() / totalWeights;
			traitChances.put(trait, chance);
		}

		return new BiasedRandomPicker<>(traitChances);
	}

	private ImmutableSet<ITrait> getEntityTraits(TraitType type, LivingEntity entity){
		if(type == null || entity == null){
			return null;
		}

		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		if(!PersistentDataContainerExtensions.hasList(pdc, type.getKey())){
			return null;
		}

		List<String> traitsArray = PersistentDataContainerExtensions.getList(pdc, type.getKey(), PersistentDataType.STRING);

		if(traitsArray == null){
			return ImmutableSet.copyOf(new ArrayList<>());
		}

		Set<ITrait> traits = new HashSet<>();
		for(String id : traitsArray){
			ITrait trait = this.getTrait(id);

			if(trait == null){
				continue;
			}

			traits.add(trait);
		}

		return ImmutableSet.copyOf(traits);
	}

	private boolean setEntityTraits(TraitType type, LivingEntity entity, ImmutableSet<ITrait> traits){
		if(type == null || entity == null || traits == null){
			return false;
		}
		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		List<String> traitsArray = new ArrayList<>();
		for(ITrait trait : traits){
			traitsArray.add(trait.getIdentifier());
		}
		PersistentDataContainerExtensions.setList(pdc, type.getKey(), PersistentDataType.STRING, traitsArray);
		return true;
	}
	 */
}

/*
5 8 2 7 4 = 26
0.192307692
 */
