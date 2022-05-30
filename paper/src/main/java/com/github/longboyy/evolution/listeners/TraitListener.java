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

	private final Map<Class<? extends Event>, Set<Consumer<Event>>> events;

	//private Map<ITrait, Map<Class<? extends Event>, Consumer<Event>>> traitEvents;

	public TraitListener(Evolution plugin){
		this.plugin = plugin;
		this.events = new HashMap<>();
		RegisteredListener registeredListener = new RegisteredListener(this, (listener, event) -> {
			onEvent(event);
		}, EventPriority.NORMAL, this.plugin, false);

		for(HandlerList handler : HandlerList.getHandlerLists()){
			handler.register(registeredListener);
			handler.bake();
		}
	}

	public void registerEvent(Class<? extends Event> eventClass, Consumer<Event> eventConsumer){
		if(!this.events.containsKey(eventClass)){
			this.events.put(eventClass, new HashSet<>());
		}

		Set<Consumer<Event>> classEvents = this.events.get(eventClass);
		classEvents.add(eventConsumer);
	}

	public void onEvent(Event event){
		if(this.events.containsKey(event.getClass())){
			Set<Consumer<Event>> classEvents = this.events.get(event.getClass());
			for(Consumer<Event> e : classEvents){
				if(e != null) {
					e.accept(event);
				}
			}
		}
	}
}
