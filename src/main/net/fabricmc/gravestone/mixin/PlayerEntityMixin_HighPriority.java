package net.fabricmc.gravestone.mixin;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.gravestone.Gravestone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

@Mixin(value = PlayerEntity.class, priority = 10)
public class PlayerEntityMixin_HighPriority {
    @Inject(method = "dropInventory", at = @At("HEAD"))
    public void onDrop(CallbackInfo ci) {
        Gravestone.CAPTURED_DROPS.set(new ArrayList<>());
    }

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"), cancellable = true)
    public void captureDrops(ItemStack stack, boolean r, boolean h, CallbackInfoReturnable<ItemEntity> cir) {
        List<ItemStack> captures = Gravestone.CAPTURED_DROPS.get();
        if(captures != null) {
            captures.add(stack);
            cir.setReturnValue(null);
        }
    }
}