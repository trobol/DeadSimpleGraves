package net.fabricmc.gravestone.access;

import java.util.List;
import java.util.UUID;

import net.minecraft.item.ItemStack;


public interface GravestoneOwnerAccess {
    void setGrave(UUID uuid, List<ItemStack> inventory);

    boolean isGrave();
}
