package com.antigravity.easylead.mixin;

import com.antigravity.easylead.LeadHolderHorse;
import net.minecraft.world.Container;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractMountInventoryMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
}
