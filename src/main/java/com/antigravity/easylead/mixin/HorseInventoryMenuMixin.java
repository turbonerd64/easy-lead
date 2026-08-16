package com.antigravity.easylead.mixin;

import com.antigravity.easylead.LeadHolderHorse;
import net.minecraft.world.Container;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractMountInventoryMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HorseInventoryMenu.class)
public abstract class HorseInventoryMenuMixin extends AbstractMountInventoryMenu {
	protected HorseInventoryMenuMixin(int containerId, Inventory playerInventory, Container mountInventory, AbstractHorse mount) {
		super(containerId, playerInventory, mountInventory, mount);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(int containerId, Inventory playerInventory, Container horseInventory, AbstractHorse horse, int inventoryColumns, CallbackInfo ci) {
		if (horse instanceof LeadHolderHorse leadHolder) {
			this.addSlot(new Slot(leadHolder.getLeadContainer(), 0, 8, 54) {
				@Override
				public boolean mayPlace(ItemStack stack) {
					return stack.is(Items.LEAD);
				}

				@Override
				public int getMaxStackSize() {
					return 1;
				}

				@Override
				public boolean isActive() {
					return horse.isAlive() && !horse.isBaby() && horse.isTamed();
				}
			});
		}
	}

	@Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
	private void onQuickMoveStack(Player player, int slotIndex, CallbackInfoReturnable<ItemStack> cir) {
		ItemStack clicked = ItemStack.EMPTY;
		Slot slot = this.slots.get(slotIndex);
		if (slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			clicked = stack.copy();

			int leadSlotIndex = this.slots.size() - 1;
			int horseChestSlotsEnd = 2 + this.mountContainer.getContainerSize();
			int playerInventoryStart = horseChestSlotsEnd;
			int playerInventoryEnd = leadSlotIndex; // player slots end before the trailing lead slot

			if (slotIndex == leadSlotIndex || slotIndex < horseChestSlotsEnd) {
				// Moving out of horse slots into player inventory
				if (!this.moveItemStackTo(stack, playerInventoryStart, playerInventoryEnd, true)) {
					cir.setReturnValue(ItemStack.EMPTY);
					return;
				}
			} else {
				// Moving from player inventory into horse slots
				boolean moved = false;
				if (stack.is(Items.LEAD) && this.getSlot(leadSlotIndex).mayPlace(stack) && !this.getSlot(leadSlotIndex).hasItem()) {
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
