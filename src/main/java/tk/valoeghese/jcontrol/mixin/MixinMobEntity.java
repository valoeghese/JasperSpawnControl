package tk.valoeghese.jcontrol.mixin;

import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import tk.valoeghese.jcontrol.JControl;

@Mixin(MobEntity.class)
public abstract class MixinMobEntity extends Entity {
	
	public MixinMobEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(at = @At("HEAD"), method = "isInDaylight", cancellable = true)
	private void mixinMobBurning(CallbackInfoReturnable<Boolean> info) {
		boolean overrideFalse = UNFLAMMABILITY_TABLE.computeIfAbsent(getType(), t -> ArrayUtils.contains(JControl.options.cannotBurn, Registry.ENTITY_TYPE.getId(t).toString()));
		if (overrideFalse) {
			info.setReturnValue(false);
		}
	}
	
	@Unique
	private static final Map<EntityType<?>, Boolean> UNFLAMMABILITY_TABLE = Maps.newHashMap();
}
