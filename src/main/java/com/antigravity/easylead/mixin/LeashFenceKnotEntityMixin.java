package com.antigravity.easylead.mixin;

import com.antigravity.easylead.EasyLead;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeashFenceKnotEntity.class)
public abstract class LeashFenceKnotEntityMixin extends BlockAttachedEntity {
	protected LeashFenceKnotEntityMixin(EntityType<? extends BlockAttachedEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "survives", at = @At("HEAD"), cancellable = true)
	private void onSurvives(CallbackInfoReturnable<Boolean> cir) {
		BlockState state = this.level().getBlockState(this.pos);
		if (EasyLead.isValidLeadAnchor(state, this.level(), this.pos)) {
			cir.setReturnValue(true);
		}
	}
}
