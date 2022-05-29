package com.github.longboyy.evolution.util;

import com.github.longboyy.evolution.Evolution;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class StringDoubleKeyEncoder implements PersistentDataType<NamespacedKey, String> {

	private final Plugin plugin;

	public StringDoubleKeyEncoder(Plugin plugin){
		this.plugin = plugin;
	}

	@Override
	public @NotNull Class<NamespacedKey> getPrimitiveType() {
		return NamespacedKey.class;
	}

	@Override
	public @NotNull Class<String> getComplexType() {
		return String.class;
	}

	@Override
	public @NotNull NamespacedKey toPrimitive(@NotNull String complex, @NotNull PersistentDataAdapterContext context) {
		return NamespacedKey.fromString(complex, plugin);
	}

	@Override
	public @NotNull String fromPrimitive(@NotNull NamespacedKey primitive, @NotNull PersistentDataAdapterContext context) {
		return primitive.getKey();
	}
}
