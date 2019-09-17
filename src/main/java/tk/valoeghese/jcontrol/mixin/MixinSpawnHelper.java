package tk.valoeghese.jcontrol.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.IWorld;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import tk.valoeghese.jcontrol.JControl;
import tk.valoeghese.jcontrol.config.Condition;
import tk.valoeghese.jcontrol.config.ConfigSpawnEntry;
import tk.valoeghese.jcontrol.config.JControlConfig;

@Mixin(SpawnHelper.class)
public class MixinSpawnHelper {

	private static BlockPos getSpawnHeight(World world, WorldChunk worldChunk) {
		ChunkPos chunkPos_1 = worldChunk.getPos();
		int spawnX = chunkPos_1.getStartX() + world.random.nextInt(16);
		int spawnZ = chunkPos_1.getStartZ() + world.random.nextInt(16);
		int heighmapY = worldChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, spawnX, spawnZ) + 1;
		int spawnY = world.random.nextInt(heighmapY + 1);
		return new BlockPos(spawnX, spawnY, spawnZ);
	}

	@Inject(at = @At("HEAD"), method = "spawnEntitiesInChunk", cancellable = true)
	private static void mixinSpawnEntitiesInChunk(EntityCategory IDontCareAboutThis, World world, WorldChunk worldChunk, BlockPos rootPos, CallbackInfo info) {
		BlockPos spawnHeightPos = getSpawnHeight(world, worldChunk);
		int baseX = spawnHeightPos.getX();
		int baseY = spawnHeightPos.getY();
		int baseZ = spawnHeightPos.getZ();
		if (baseY >= 1) {
			BlockState blockState_1 = worldChunk.getBlockState(spawnHeightPos);
			if (!blockState_1.isSimpleFullBlock(worldChunk, spawnHeightPos)) {
				int x = baseX;
				int z = baseZ;

				for (JControlConfig config : JControl.configs) {
					EntityType<?> type = Registry.ENTITY_TYPE.get(new Identifier(config.entity));

					BlockPos.Mutable mutablePos = new BlockPos.Mutable(baseX, baseY, baseZ);
					
					int entityCount = 0;
					
					for (ConfigSpawnEntry entry : config.spawns) {
						boolean breakSpawnEntries = false;
						
						if ((world.random.nextFloat() < entry.chance) && Condition.get(new Identifier(entry.condition)).test(world, baseX, baseY, baseZ, entry.config)) {
							EntityData entityData = null;
							
							int spawnCount = world.random.nextInt(1 + entry.maxCount - entry.minCount) + entry.minCount;
							entityCount += spawnCount;
							
							for (int i = 0; i < spawnCount; ++i) {
								x += world.random.nextInt(6) - world.random.nextInt(6);
								z += world.random.nextInt(6) - world.random.nextInt(6);
								float xFloat = (float)x + 0.5F;
								float zFloat = (float)z + 0.5F;
								mutablePos.setX(x);
								mutablePos.setZ(z);

								PlayerEntity player = world.getClosestPlayer((double)xFloat, (double)zFloat, -1.0D);
								if (player == null) {
									break;
								}

								double squaredDistanceToPlayer = player.squaredDistanceTo((double)xFloat, (double)baseY, (double)zFloat);
								if (squaredDistanceToPlayer <= 576.0D || rootPos.isWithinDistance(new Vec3d((double)xFloat, (double)baseY, (double)zFloat), 24.0D)) {
									break;
								}

								SpawnRestriction.Location spawnRestrictionLocation = SpawnRestriction.getLocation(type);
								boolean defaultComputedConditions = entry.defaultConditions && !SpawnRestriction.method_20638(type, world, SpawnType.NATURAL, mutablePos, world.random);

								if (!SpawnHelper.canSpawn(spawnRestrictionLocation, world, mutablePos, type) || defaultComputedConditions || !world.doesNotCollide(type.createSimpleBoundingBox((double)xFloat, (double)baseY, (double)zFloat))) {
									break;
								}

								MobEntity entity;
								try {
									Entity createdEntity = type.create(world);
									if (!(createdEntity instanceof MobEntity)) {
										throw new IllegalStateException("[JControl] Trying to spawn a non-mob: " + Registry.ENTITY_TYPE.getId(type));
									}

									entity = (MobEntity)createdEntity;
									
								} catch (Exception var31) {
									System.err.println("Failed to create mob " + var31);
									return;
								}

								entity.setPositionAndAngles((double)xFloat, (double)baseY, (double)zFloat, world.random.nextFloat() * 360.0F, 0.0F);

								boolean cannotSpawnEntity = !canSpawnEntity(entity, world, SpawnType.NATURAL, entry);

								if ((squaredDistanceToPlayer > 16384.0D && entity.canImmediatelyDespawn(squaredDistanceToPlayer)) || cannotSpawnEntity || !entity.canSpawn(world)) {
									break;
								}

								entityData = entity.initialize(world, world.getLocalDifficulty(new BlockPos(entity)), SpawnType.NATURAL, entityData, (CompoundTag)null);

								world.spawnEntity(entity);
								
								if (i == spawnCount - 1) {
									if (entityCount >= entity.getLimitPerChunk()) {
										breakSpawnEntries = true;
										break;
									}
								}
							}
							
							if (breakSpawnEntries) {
								break;
							}
						}
					}
				}
			}
		}
		info.cancel();
	}

	private static boolean canSpawnEntity(MobEntity entity, World world, SpawnType natural, ConfigSpawnEntry entry) {
		boolean forceTrue = !entry.defaultConditions;

		return forceTrue || entity.canSpawn(world, SpawnType.NATURAL);
	}

	@Inject(at = @At("HEAD"), method = "populateEntities", cancellable = true)
	private static void mixinPopulateEntities(IWorld world, Biome biome, int x, int z, Random rand, CallbackInfo info) {
		info.cancel();
	}
}
