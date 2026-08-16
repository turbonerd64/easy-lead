package com.antigravity.easylead.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractMountInventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMountInventoryMenu.class)
public abstract class AbstractMountInventoryMenuMixin extends AbstractContainerMenu {
	@Shadow
	@Final
	protected Container mountContainer;

	protected AbstractMountInventoryMenuMixin() {
		super(null, 0);
	}

	@Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
	private void onQuickMoveStack(Player player, int slotIndex, CallbackInfoReturnable<ItemStack> cir) {
		Slot slot = this.slots.get(slotIndex);
		if (slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			ItemStack clicked = stack.copy();

			int totalSlots = this.slots.size();
			int horseChestSlotsEnd = 2 + this.mountContainer.getContainerSize();
			boolean hasLeadSlot = totalSlots > (horseChestSlotsEnd + 36);
			int leadSlotIndex = hasLeadSlot ? totalSlots - 1 : -1;
			int playerInventoryStart = horseChestSlotsEnd;
			int playerInventoryEnd = hasLeadSlot ? leadSlotIndex : totalSlots;

			if (slotIndex == leadSlotIndex || slotIndex < horseChestSlotsEnd) {
				// Move from mount slots into player inventory
				if (!this.moveItemStackTo(stack, playerInventoryStart, playerInventoryEnd, true)) {
					cir.setReturnValue(ItemStack.EMPTY);
					return;
				}
			} else {
				// Move from player inventory into mount slots
				boolean moved = false;
				if (hasLeadSlot && stack.is(Items.LEAD) && this.getSlot(leadSlotIndex).mayPlace(stack) && !this.getSlot(leadSlotIndex).hasItem()) {
					moved = this.moveItemStackTo(stack, leadSlotIndex, leadSlotIndex + 1, false);
				} else if (this.getSlot(0).mayPlace(stack) && !this.getSlot(0).hasItem()) {
					moved = this.moveItemStackTo(stack, 0, 1, false);
				} else if (this.getSlot(1).mayPlace(stack) && !this.getSlot(1).hasItem()) {
					moved = this.moveItemStackTo(stack, 1, 2, false);
				} else if (this.mountContainer.getContainerSize() > 0) {
					moved = this.moveItemStackTo(stack, 2, horseChestSlotsEnd, false);
				}

				if (!moved) {
					int playerMainEnd = playerInventoryStart + 27;
					int playerHotbarEnd = playerInventoryEnd;
					if (slotIndex >= playerInventoryStart && slotIndex < playerMainEnd) {
						if (!this.moveItemStackTo(stack, playerMainEnd, playerHotbarEnd, false)) {
							cir.setReturnValue(ItemStack.EMPTY);
							return;
						}
					} else if (slotIndex >= playerMainEnd && slotIndex < playerHotbarEnd) {
						if (!this.moveItemStackTo(stack, playerInventoryStart, playerMainEnd, false)) {
							cir.setReturnValue(ItemStack.EMPTY);
							return;
						}
					} else {
						cir.setReturnValue(ItemStack.EMPTY);
						return;
					}
				}
			}

			if (stack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			cir.setReturnValue(clicked);
		}
	}
}
