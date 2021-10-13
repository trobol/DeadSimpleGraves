package net.fabricmc.gravestone.mixin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.gravestone.Gravestone;
import net.fabricmc.gravestone.access.GravestoneOwnerAccess;
import net.fabricmc.gravestone.access.RestoreAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin (value = PlayerEntity.class, priority = 100000)
public abstract class PlayerEntityMixin_NormalPriority extends LivingEntity implements RestoreAccess {
    @Unique protected final Map<UUID, List<ItemStack>> ueaj_restores = new HashMap<>();

    protected PlayerEntityMixin_NormalPriority(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject (method = "readCustomDataFromNbt", at = @At ("HEAD"))
    public void read(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound restores = nbt.getCompound("ueaj:restores");
        if (restores != null) {
            for (String key : restores.getKeys()) {
                NbtList items = (NbtList) restores.get(key);
                List<ItemStack> stacks = new ArrayList<>();
                for (NbtElement item : items) {
                    stacks.add(ItemStack.fromNbt((NbtCompound) item));
                }
                this.ueaj_restores.put(UUID.fromString(key), stacks);
            }
        }
    }

    @Inject (method = "writeCustomDataToNbt", at = @At ("HEAD"))
    public void write(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound restores = new NbtCompound();
        for (Map.Entry<UUID, List<ItemStack>> entry : this.ueaj_restores.entrySet()) {
            List<ItemStack> stacks = entry.getValue();
            NbtList items = new NbtList();
            for (ItemStack stack : stacks) {
                items.add(stack.writeNbt(new NbtCompound()));
            }
            restores.put(entry.getKey().toString(), items);
        }
        nbt.put("ueaj:restores", restores);
    }

    @Inject (method = "dropInventory", at = @At ("RETURN"))
    public void onDropInventory(CallbackInfo ci) {
        List<ItemStack> capturedDrops = Gravestone.CAPTURED_DROPS.get();
        World world = this.getEntityWorld();
        BlockPos deathPos = this.getBlockPos();
        if (world.setBlockState(deathPos, Blocks.BARREL.getDefaultState())) {
            BlockEntity chest = world.getBlockEntity(deathPos);
            if (chest instanceof GravestoneOwnerAccess o) {
                o.setGrave(this.getUuid(), capturedDrops);
            } else {
                this.ueaj_restore(capturedDrops);
            }
        } else {
            this.ueaj_restore(capturedDrops);
        }

        Gravestone.CAPTURED_DROPS.remove();
    }

    @Unique
    public void ueaj_restore(List<ItemStack> capturedDrops) {
        UUID restoreId = UUID.randomUUID();
        this.ueaj_restores.put(restoreId, capturedDrops);
        this.sendMessage(new LiteralText("/ueaj_restore " + restoreId).styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                "/ueaj_restore " + restoreId))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new LiteralText(
                                        "/ueaj_restore " + restoreId)))),
                false);
    }

    @Shadow
    public abstract void sendMessage(Text message, boolean actionBar);

    @Override
    public Map<UUID, List<ItemStack>> getRestores() {
        return this.ueaj_restores;
    }
}