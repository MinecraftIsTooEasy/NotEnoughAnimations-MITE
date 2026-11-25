package org.moddedmite.notenoughanimations.mixin;

import net.minecraft.Entity;
import net.minecraft.ModelBiped;
import org.moddedmite.notenoughanimations.ThirdPersonAnimator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies the action layer after vanilla has established the base player pose. */
// Run after the default-priority offhand ModelBiped tail hooks so the saved
// snapshot sees the final native pose before capturing NEA-owned actions.
@Mixin(value = ModelBiped.class, priority = 900)
public final class ModelBipedMixin {

    @Inject(method = "setRotationAngles", at = @At("TAIL"))
    private void applyUsePose(float limbSwing, float limbSwingAmount, float age,
                              float headYaw, float headPitch, float scale,
                              Entity entity, CallbackInfo callbackInfo) {
        ModelBiped model = (ModelBiped) (Object) this;
        // Capture the actual vanilla/NEA result before EMF evaluates CEM. The
        // external pose hook later restores only active parts, leaving FA's
        // CEM locomotion authoritative everywhere else.
        ThirdPersonAnimator.captureVanillaPose(model, entity, age - entity.ticksExisted);
    }
}
