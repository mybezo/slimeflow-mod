package com.mybezo.macro.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.platform.Window;

@Mixin(Window.class)
public interface WindowAccessor {
	@Accessor("handle")
	long slimeflow$getHandle();
}
