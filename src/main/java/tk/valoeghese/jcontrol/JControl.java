package tk.valoeghese.jcontrol;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import tk.valoeghese.jcontrol.config.ConditionRegistry;
import tk.valoeghese.jcontrol.config.JControlConfig;
import tk.valoeghese.jcontrol.config.ConditionRegistry.ConditionConfigPair;

public class JControl implements ModInitializer {

	public static File dir = new File("./jcontrol");
	public static List<JControlConfig> configs = Lists.<JControlConfig>newArrayList();
	public static JControlOptions options = new JControlOptions();

	@Override
	public void onInitialize() {
		if (!dir.exists()) {
			dir.mkdir();
		}

		Gson gson = new Gson();
		for (File f : dir.listFiles(f -> f.getName().endsWith(".json"))) {
			try (FileReader reader = new FileReader(f)) {
				if (f.getName().equals("options.json")) {
					options = gson.fromJson(reader, JControlOptions.class);
				} else {
					configs.add(gson.fromJson(reader, JControlConfig.class));
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		registerConditions();
	}

	private void registerConditions() {
		// locational conditions
		ConditionRegistry.register("jcontrol:on_block", (world, x, y, z, config) -> {
			BlockPos pos = new BlockPos(x, y == 0 ? y : y - 1, z);
			Block blockOn = Registry.BLOCK.get(new Identifier(config.id));
			return world.getBlockState(pos).getBlock().equals(blockOn);
		});
		ConditionRegistry.register("jcontrol:in_biome", (world, x, y, z, config) -> {
			BlockPos pos = new BlockPos(x, y, z);
			Biome biome = Registry.BIOME.get(new Identifier(config.id));
			return world.getBiome(pos).equals(biome);
		});

		ConditionRegistry.register("jcontrol:below_y", (world, x, y, z, config) -> {
			return y < config.value;
		});
		ConditionRegistry.register("jcontrol:above_y", (world, x, y, z, config) -> {
			return y > config.value;
		});

		// special chunk
		ConditionRegistry.register("jcontrol:special_chunk_16", (world, x, y, z, config) -> {
			int random = (-56235 * (x >> 4) + 94231 * (z >> 4)) & 0xF;
			return (random == (int) config.value);
		});
		ConditionRegistry.register("jcontrol:special_chunk_8", (world, x, y, z, config) -> {
			int random = (-56235 * (x >> 4) + 94231 * (z >> 4)) & 0x7;
			return (random == (int) config.value);
		});

		// range
		ConditionRegistry.register("jcontrol:near_position", (world, x, y, z, config) -> {
			Vec3d pos = new Vec3d(x, y, z);
			Vec3d point = new Vec3d(config.position[0], config.position[1], config.position[2]);

			return (point.distanceTo(pos) < config.value);
		});

		// multi
		ConditionRegistry.register("jcontrol:multi_and", (world, x, y, z, config) -> {
			for (ConditionConfigPair condition : config.conditions) {
				if (!ConditionRegistry.get(new Identifier(condition.condition)).test(world, x, y, z, condition.config)) {
					return false;
				}
			}
			return true;
		});

		ConditionRegistry.register("jcontrol:multi_or", (world, x, y, z, config) -> {
			for (ConditionConfigPair condition : config.conditions) {
				if (ConditionRegistry.get(new Identifier(condition.condition)).test(world, x, y, z, condition.config)) {
					return true;
				}
			}
			return false;
		});

		// light level
		ConditionRegistry.register("jcontrol:light_level_min", (world, x, y, z, config) -> {
			return world.getLightLevel(new BlockPos(x, y, z)) >= config.value;
		});

		ConditionRegistry.register("jcontrol:light_level_max", (world, x, y, z, config) -> {
			return world.getLightLevel(new BlockPos(x, y, z)) <= config.value;
		});
	}

	public static class JControlOptions {
		public final String[] cannotBurn = new String[0];
		public final boolean letVanillaPopulateChunk = false;
	}

}
