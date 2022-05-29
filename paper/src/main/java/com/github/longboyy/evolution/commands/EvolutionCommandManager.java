package com.github.longboyy.evolution.commands;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.CommandContexts;
import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.traits.ITrait;
import com.github.longboyy.evolution.traits.TraitManager;
import com.github.longboyy.evolution.traits.TraitType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.commands.CommandManager;

import java.util.Arrays;
import java.util.List;

public class EvolutionCommandManager extends CommandManager {
	/**
	 * Creates a new command manager for Aikar based commands and tab completions.
	 *
	 * @param plugin The plugin to bind this manager to.
	 */
	public EvolutionCommandManager(@NotNull Plugin plugin) {
		super(plugin);
		//this.init();
		this.registerContexts(this.getCommandContexts());
		this.registerCompletions(this.getCommandCompletions());
		this.registerCommands();
		//this.getCommandConditions().
	}

	@Override
	public void registerCommands() {
		super.registerCommands();
		this.registerCommand(new EvolutionCommand(Evolution.getInstance()));
	}

	@Override
	public void registerCompletions(@NotNull CommandCompletions<BukkitCommandCompletionContext> completions) {
		super.registerCompletions(completions);
		completions.registerCompletion("EvoTraits", context -> {
			TraitManager manager = Evolution.getInstance().getTraitManager();
			return manager.getTraits().stream().map(trait -> trait.getIdentifier()).toList();
		});
		completions.registerCompletion("Boolean", context -> Arrays.asList("true", "false", "1", "0", "yes", "no"));
	}

	@Override
	public void registerContexts(@NotNull CommandContexts<BukkitCommandExecutionContext> contexts) {
		super.registerContexts(contexts);
		contexts.registerContext(ITrait.class, context -> {
			List<String> args = context.getArgs();
			if(args.isEmpty()){
				return null;
			}

			ITrait trait = Evolution.getInstance().getTraitManager().getTrait(args.get(0));
			return trait;
		});

		contexts.registerContext(TraitType.class, context -> {
			List<String> args = context.getArgs();
			if(args.isEmpty()){
				return null;
			}

			try {
				return TraitType.valueOf(args.get(0));
			}catch(IllegalArgumentException e){
				return null;
			}
		});
	}
}
