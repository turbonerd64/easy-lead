package com.antigravity.easylead;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public interface LeadHolderHorse {
	Container getLeadContainer();

	ItemStack getEquippedLead();

	void setEquippedLead(ItemStack stack);

	boolean hasEquippedLead();

	boolean isLeashedFromEquippedLead();

	void setLeashedFromEquippedLead(boolean value);
}
