package com.antigravity.easylead.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractMountInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractMountInventoryMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMountInventoryScreen.class)
public abstract class AbstractMountInventoryScreenMixin<T extends AbstractMountInventoryMenu> extends AbstractContainerScreen<T> {
	@Shadow
	@Final
	protected LivingEntity mount;

	@Shadow
	protected abstract void extractSlot(GuiGraphicsExtractor graphics, int x, int y);

	protected AbstractMountInventoryScreenMixin(T menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}

	@Inject(method = "extractBackground", at = @At("RETURN"))
	private void onExtractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		if (this.mount instanceof AbstractHorse horse && horse.isAlive() && !horse.isBaby() && horse.isTamed()) {
			int xo = (this.width - this.imageWidth) / 2;
			int yo = (this.height - this.imageHeight) / 2;
			this.extractSlot(graphics, xo + 7, yo + 53);
		}
	}
}
