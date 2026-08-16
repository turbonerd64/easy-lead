package com.antigravity.easylead.mixin;

import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockAttachedEntity.class)
public abstract class BlockAttachedEntityMixin {
	@Inject(method = "move", at = @At("HEAD"), cancellable = true)
	private void onMove(MoverType moverType, Vec3 delta, CallbackInfo ci) {
		if ((Object) this instanceof LeashFenceKnotEntity) {
			ci.cancel();
		}
	}

	@Inject(method = "push", at = @At("HEAD"), cancellable = true)
	private void onPush(double xa, double ya, double za, CallbackInfo ci) {
		if ((Object) this instanceof LeashFenceKnotEntity) {
			ci.cancel();
		}
	}
}
