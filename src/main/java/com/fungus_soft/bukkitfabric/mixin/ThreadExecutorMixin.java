package com.fungus_soft.bukkitfabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.fungus_soft.bukkitfabric.interfaces.IMixinThreadExecutor;

import net.minecraft.util.thread.ThreadExecutor;

@Mixin(ThreadExecutor.class)
public class ThreadExecutorMixin implements IMixinThreadExecutor {

    @Override
    @Shadow
    public void runTasks() {}

}