package net.fabricmc.gravestone.mixin;


import java.util.List;
import java.util.UUID;

import net.fabricmc.gravestone.access.GravestoneOwnerAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;

@Mixin (BarrelBlockEntity.class)
public abstract class BarrelBlockEntityMixin implements GravestoneOwnerAccess, Inventory {
    public UUID ueaj_graveOwner;
    @Shadow private DefaultedList<ItemStack> inventory;

    @Inject (method = "readNbt", at = @At ("HEAD"))
    public void readGraveOwner(NbtCompound nbt, CallbackInfo ci) {
        NbtElement element = nbt.get("ueajs_graves:graveOwnerUuid");
        if (element != null) {
            NbtHelper.toUuid(element);
        }
        int i = nbt.getInt("ueajs_graves:invsize");
        if (i != 0) {
            this.inventory = DefaultedList.ofSize(i, ItemStack.EMPTY);
        }
    }

    @Inject (method = "writeNbt", at = @At ("HEAD"))
    public void writeGraveOwner(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        UUID owner = this.ueaj_graveOwner;
        if (owner != null) {
            nbt.put("ueajs_graves:graveOwnerUuid", NbtHelper.fromUuid(owner));
        }
        nbt.putInt("ueajs_graves:invsize", this.inventory.size());
    }

    @Inject (method = "createScreenHandler", at = @At ("HEAD"), cancellable = true)
    public void customGrave(int syncId, PlayerInventory playerInventory, CallbackInfoReturnable<ScreenHandler> cir) {
        if (this.inventory.size() == 54) {
            cir.setReturnValue(GenericContainerScreenHandler.createGeneric9x6(syncId, playerInventory, this));
        }
    }

    @Override
    public void setGrave(UUID uuid, List<ItemStack> inventory) {
        this.ueaj_graveOwner = uuid;
        if (inventory.size() > 27) {
            this.inventory = DefaultedList.ofSize(54, ItemStack.EMPTY);
        }
        for (int i = 0; i < Math.min(inventory.size(), 54); i++) {
            this.inventory.set(i, inventory.get(i));
        }
    }

    @Override
    public boolean isGrave() {
        return this.ueaj_graveOwner != null;
    }

    /**
     * @author HalfOf2
     * @reason use actual size
     */
    @Override
    @Overwrite
    public int size() {
        return this.inventory.size();
    }
}
