package com.antigravity.easylead.mixin;

import com.antigravity.easylead.LeadHolderHorse;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin extends Animal implements LeadHolderHorse {
	@Unique
	private final SimpleContainer easylead$leadContainer = new SimpleContainer(1);

	@Unique
	private boolean easylead$leashedFromEquippedLead = false;

	protected AbstractHorseMixin(EntityType<? extends Animal> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public Container getLeadContainer() {
		return this.easylead$leadContainer;
	}

	@Override
	public ItemStack getEquippedLead() {
		return this.easylead$leadContainer.getItem(0);
	}

	@Override
	public void setEquippedLead(ItemStack stack) {
		this.easylead$leadContainer.setItem(0, stack);
	}

	@Override
	public boolean hasEquippedLead() {
		return !this.easylead$leadContainer.getItem(0).isEmpty() && this.easylead$leadContainer.getItem(0).is(Items.LEAD);
	}

	@Override
	public boolean isLeashedFromEquippedLead() {
		return this.easylead$leashedFromEquippedLead;
	}

	@Override
	public void setLeashedFromEquippedLead(boolean value) {
		this.easylead$leashedFromEquippedLead = value;
	}

	@Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
	private void onAddAdditionalSaveData(ValueOutput output, CallbackInfo ci) {
		ItemStack lead = this.easylead$leadContainer.getItem(0);
		if (!lead.isEmpty()) {
			output.store("EquippedLead", ItemStack.CODEC, lead);
		}
		output.putBoolean("LeashedFromEquippedLead", this.easylead$leashedFromEquippedLead);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
	private void onReadAdditionalSaveData(ValueInput input, CallbackInfo ci) {
		this.easylead$leadContainer.setItem(0, input.read("EquippedLead", ItemStack.CODEC).orElse(ItemStack.EMPTY));
		this.easylead$leashedFromEquippedLead = input.getBooleanOr("LeashedFromEquippedLead", false);
	}

	@Inject(method = "dropEquipment", at = @At("HEAD"))
	private void onDropEquipment(ServerLevel level, CallbackInfo ci) {
		ItemStack lead = this.easylead$leadContainer.getItem(0);
		if (!lead.isEmpty()) {
			this.spawnAtLocation(level, lead);
			this.easylead$leadContainer.setItem(0, ItemStack.EMPTY);
		}
	}
}
