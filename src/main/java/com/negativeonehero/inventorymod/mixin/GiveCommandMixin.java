package com.negativeonehero.inventorymod.mixin;

import net.minecraft.server.command.GiveCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(GiveCommand.class)
public class GiveCommandMixin {

    @ModifyConstant(method = "execute", constant = @Constant(intValue = 100))
    private static int maxStacks(int stacks) { return 1; }
}
