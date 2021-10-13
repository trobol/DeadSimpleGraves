package net.fabricmc.gravestone;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.command.argument.UuidArgumentType.uuid;

import java.util.List;
import java.util.UUID;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.gravestone.access.RestoreAccess;

import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class DeadSimpleGraves implements ModInitializer {
    public static final ThreadLocal<List<ItemStack>> CAPTURED_DROPS = new ThreadLocal<>();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("dsg_restore")
                    .then(RequiredArgumentBuilder.<ServerCommandSource, UUID>argument("restore_id", uuid())
                            .executes(context -> {
                                UUID id = context.getArgument("restore_id", UUID.class);
                                ServerPlayerEntity p = context.getSource().getPlayer();
                                RestoreAccess access = (RestoreAccess) p;
                                List<ItemStack> stacks = access.getRestores().remove(id);
                                if (stacks != null) {
                                    for (ItemStack stack : stacks) {
                                        p.getInventory().offerOrDrop(stack);
                                    }
                                    return 1;
                                }

                                return 0;
                            })));
        });
    }
}


