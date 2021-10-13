package net.fabricmc.gravestone.mixin;

import net.fabricmc.gravestone.access.RestoreAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin_CopyTo implements RestoreAccess {
    @Inject(method = "copyFrom", at = @At("HEAD"))
    public void copy(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        this.getRestores().putAll(((RestoreAccess)oldPlayer).getRestores());
    }
}