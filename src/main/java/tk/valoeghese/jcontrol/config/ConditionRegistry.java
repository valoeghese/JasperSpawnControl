package tk.valoeghese.jcontrol.config;

import java.util.function.BiConsumer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.util.Identifier;

public final class ConditionRegistry {
	private static final BiMap<Identifier, Condition> CONDITIONS = HashBiMap.create();
	
	public static <T extends Condition> T register(Identifier id, T c) {
		CONDITIONS.put(id, c);
		return c;
	}
	
	public static <T extends Condition> T register(String id, T c) {
		return register(new Identifier(id), c);
	}
	
	public static Condition get(Identifier i) {
		return CONDITIONS.get(i);
	}
	
	public static Identifier getId(Condition c) {
		return CONDITIONS.inverse().get(c);
	}
	
	public static void forEach(BiConsumer<Identifier, Condition> callback) {
		CONDITIONS.forEach(callback);
	}
	
	public static class ConditionConfig {
		public final double value = 0.0D;
		public final int[] position = {0, 0, 0};
		public final String id = "minecraft:none";
		public final ConditionConfigPair[] conditions = new ConditionConfigPair[0];
	}
	
	public static class ConditionConfigPair {
		public final String condition = null;
		public final ConditionConfig config = null;
	}
	
	static {
		register("minecraft:none", (world, x, y, z, config) -> true);
	}
}
