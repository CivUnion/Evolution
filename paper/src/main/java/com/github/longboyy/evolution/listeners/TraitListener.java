package com.github.longboyy.evolution.listeners;

import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.traits.ITrait;
import com.github.longboyy.evolution.traits.TraitManager;
import com.github.longboyy.evolution.traits.TraitType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class TraitListener implements Listener {

	private final Evolution plugin;
	private final TraitManager traitManager;

	private final Map<Class<? extends Event>, Set<Consumer<Event>>> events;

	//private Map<ITrait, Map<Class<? extends Event>, Consumer<Event>>> traitEvents;

	public TraitListener(Evolution plugin, TraitManager manager){
		this.plugin = plugin;
		this.traitManager = manager;
		//this.traitEvents = new HashMap<>();
		this.events = new HashMap<>();
		RegisteredListener registeredListener = new RegisteredListener(this, (listener, event) -> {
			//this.plugin.info(String.format("Attempting to execute event '%s'", event.getEventName()));
			onEvent(event);
		}, EventPriority.NORMAL, this.plugin, false);

		for(HandlerList handler : HandlerList.getHandlerLists()){
			handler.register(registeredListener);
			handler.bake();
		}

		/*
		Bukkit.getPluginManager().registerEvent(EntityEvent.class, this, EventPriority.NORMAL, (listener, event) -> {
			this.onEvent((EntityEvent) event);
		}, this.plugin, false);
		 */
	}

	public void registerEvent(Class<? extends Event> eventClass, Consumer<Event> eventConsumer){
		if(!this.events.containsKey(eventClass)){
			this.events.put(eventClass, new HashSet<>());
		}

		Set<Consumer<Event>> classEvents = this.events.get(eventClass);
		classEvents.add(eventConsumer);
	}

	/*
	public void registerEvent(ITrait trait, Class<? extends Event> eventClass, Consumer<Event> eventCallback){
		if(!this.events.containsKey(trait)){
			this.events.put(trait, new HashMap<>());
		}

		Map<Class<? extends Event>, Consumer<Event>> events = this.traitEvents.get(trait);
		events.put(eventClass, eventCallback);
	}
	 */

	/*
	@EventHandler
	public void onEntityMove(EntityMoveEvent event){
		LivingEntity entity = event.getEntity();
		Evolution.getInstance().info(String.format("Entity '%s'(%s[%s]) moved from %s to %s",
				entity.getName(),
				entity.getType(),
				entity.getUniqueId(),
				event.getFrom(),
				event.getTo())
		);
	}
	 */

	public void onEvent(Event event){
		if(this.events.containsKey(event.getClass())){
			Set<Consumer<Event>> classEvents = this.events.get(event.getClass());
			for(Consumer<Event> e : classEvents){
				if(e != null) {
					e.accept(event);
				}
			}
		}

		/*
		if(event.getEntity() instanceof LivingEntity){
			LivingEntity entity = (LivingEntity)event.getEntity();
			ImmutableMap<ITrait, TraitType> traits = this.traitManager.getAllTraitsOf(entity);
			if(traits == null){
				return;
			}
			for(ITrait trait : traits.keySet()){

				Map<Class<? extends Event>, Consumer<Event>> events = this.traitEvents.get(trait);
				if(events != null && events.containsKey(event.getClass())){
					Consumer<EntityEvent> e = events.get(event.getClass());
					//this.plugin.info(String.format("Attempting to execute event '%s'", event.getClass()));
					if(e != null){
						this.plugin.info(String.format("Executing event '%s'", event.getClass()));
						e.accept(event);
					}
				}

			}
		}
		 */

	}
}
