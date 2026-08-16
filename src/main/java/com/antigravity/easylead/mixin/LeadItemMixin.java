package com.antigravity.easylead.mixin;

import com.antigravity.easylead.EasyLead;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeadItem.class)
public abstract class LeadItemMixin extends Item {
	public LeadItemMixin(Item.Properties properties) {
		super(properties);
	}

	@Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
	private void onUseOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);

		if (EasyLead.isValidLeadAnchor(state, level, pos)) {
			Player player = context.getPlayer();
			if (player != null) {
				if (!level.isClientSide()) {
					InteractionResult result = LeadItem.bindPlayerMobs(player, level, pos);
					if (result.consumesAction()) {
						cir.setReturnValue(result);
					}
				} else {
					List<Leashable> entitiesToLeash = Leashable.leashableInArea(
						level, Vec3.atCenterOf(pos), l -> l.getLeashHolder() == player
					);
					if (!entitiesToLeash.isEmpty()) {
						cir.setReturnValue(InteractionResult.SUCCESS);
					}
				}
			}
		}
	}
}
