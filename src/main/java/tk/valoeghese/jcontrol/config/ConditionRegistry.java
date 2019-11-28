package tk.valoeghese.jcontrol.config;

import java.util.function.BiConsumer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.util.Identifier;

public final class ConditionRegistry {
	private static final BiMap<Identifier, Condition> conditions = HashBiMap.create();
	
	public static final <T extends Condition> T register(Identifier id, T c) {
		conditions.put(id, c);
		return c;
	}
	
	public static final <T extends Condition> T register(String id, T c) {
		return register(new Identifier(id), c);
	}
	
	public static Condition get(Identifier i) {
		return conditions.get(i);
	}
	
	public static Identifier getId(Condition c) {
		return conditions.inverse().get(c);
	}
	
	public static void forEach(BiConsumer<Identifier, Condition> callback) {
		conditions.forEach(callback);
	}
	
	public static class ConditionConfig {
		public double value = 0.0D;
		public int[] position = {0, 0, 0};
		public String id = "minecraft:none";
		public ConditionConfigPair[] conditions = new ConditionConfigPair[0];
	}
	
	public static class ConditionConfigPair {
		public String condition;
		public ConditionConfig config;
	}
	
	static {
		register("minecraft:none", (world, x, y, z, config) -> true);
	}
}
