package com.antigravity.easylead;

import java.util.List;
import java.util.Optional;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyLead implements ModInitializer {
	public static final String MOD_ID = "easylead";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final TagKey<Block> LEASH_ANCHORS = TagKey.create(
		Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "leash_anchors")
	);
	public static final TagKey<Block> LEASH_ANCHORS_BLACKLIST = TagKey.create(
		Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "leash_anchors_blacklist")
	);

	@Override
	public void onInitialize() {
		LOGGER.info("Easy Lead initialized with Horse Lead Slot & Quick-Tethering!");

		// Intercept right-click block interaction for mounted tethering, hand tethering, and untethering
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (player.isSpectator()) {
				return InteractionResult.PASS;
			}

			BlockPos pos = hitResult.getBlockPos();
			BlockState state = world.getBlockState(pos);

			if (isValidLeadAnchor(state, world, pos)) {
				// Don't intercept when opening interactive container blocks (chests, furnaces, crafting tables) unless sneaking
				if (!player.isSecondaryUseActive() && state.getMenuProvider(world, pos) != null) {
					return InteractionResult.PASS;
				}

				// Case 1: Player is riding a horse that has a lead equipped in its lead slot -> Quick-Tether from saddle!
				Entity vehicle = player.getVehicle();
				if (vehicle instanceof AbstractHorse horse && horse instanceof LeadHolderHorse leadHorse && leadHorse.hasEquippedLead()) {
					if (!world.isClientSide()) {
						player.stopRiding();
						LeashFenceKnotEntity activeKnot = LeashFenceKnotEntity.getOrCreateKnot(world, pos);
						horse.setLeashedTo(activeKnot, true);
						leadHorse.setLeashedFromEquippedLead(true);
						activeKnot.playPlacementSound();
						world.gameEvent(GameEvent.BLOCK_ATTACH, pos, GameEvent.Context.of(player));
						return InteractionResult.SUCCESS_SERVER;
					} else {
						return InteractionResult.SUCCESS;
					}
				}

				// Case 2: Player is holding leashed animals on foot -> Tether them to this block
				List<Leashable> playerLeashedMobs = Leashable.leashableLeashedTo(player);
				if (!playerLeashedMobs.isEmpty()) {
					if (!world.isClientSide()) {
						InteractionResult result = LeadItem.bindPlayerMobs(player, world, pos);
						if (result.consumesAction()) {
							return result;
						}
					} else {
						return InteractionResult.SUCCESS;
					}
				}

				// Case 3: Player is on foot and right-clicks an anchor block that has tethered animals -> Untether
				Optional<LeashFenceKnotEntity> knot = LeashFenceKnotEntity.getKnot(world, pos);
				if (knot.isPresent()) {
					LeashFenceKnotEntity activeKnot = knot.get();
					List<Leashable> knotMobs = Leashable.leashableLeashedTo(activeKnot);

					if (!knotMobs.isEmpty() && !player.isSecondaryUseActive()) {
						if (!world.isClientSide()) {
							boolean untethered = false;
							for (Leashable mob : knotMobs) {
								if (mob instanceof AbstractHorse horse && horse instanceof LeadHolderHorse leadHorse && leadHorse.hasEquippedLead()) {
									// Horse with equipped lead slot: untether cleanly without spawning loose item
									mob.dropLeash();
									leadHorse.setLeashedFromEquippedLead(false);
									untethered = true;
								} else if (mob.canHaveALeashAttachedTo(player)) {
									// Standard mob: transfer leash to player's hand
									mob.setLeashedTo(player, true);
									untethered = true;
								}
							}

							if (untethered) {
								activeKnot.playSound(SoundEvents.LEAD_TIED);
								world.gameEvent(GameEvent.BLOCK_ATTACH, pos, GameEvent.Context.of(player));
								return InteractionResult.SUCCESS_SERVER;
							}
						} else {
							return InteractionResult.SUCCESS;
						}
					}
				}
			}

			return InteractionResult.PASS;
		});

		// Handle block breaking: If the anchor block is mined by a player, cleanly detach animals and drop leads.
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (!world.isClientSide() && world instanceof ServerLevel serverLevel) {
				Optional<LeashFenceKnotEntity> knot = LeashFenceKnotEntity.getKnot(serverLevel, pos);
				knot.ifPresent(k -> {
					if (!k.isRemoved() && !k.survives()) {
						for (Leashable mob : Leashable.leashableLeashedTo(k)) {
							mob.dropLeash();
						}
						k.discard();
					}
				});
			}
		});
	}

	/**
	 * Determines whether a block can host a leash knot attachment.
	 */
	public static boolean isValidLeadAnchor(final BlockState state, final Level level, final BlockPos pos) {
		if (state.is(LEASH_ANCHORS_BLACKLIST)) {
			return false;
		}

		if (state.is(LEASH_ANCHORS) || state.is(BlockTags.FENCES) || state.is(BlockTags.WALLS)) {
			return true;
		}

		if (state.isAir()) {
			return false;
		}

		// Ensure the block has physical collision or solid structure (not air, water, fire, tall grass, torch, etc.)
		try {
			return !state.getCollisionShape(level, pos).isEmpty() || state.isSolid();
		} catch (Exception e) {
			return !state.isAir();
		}
	}
}
