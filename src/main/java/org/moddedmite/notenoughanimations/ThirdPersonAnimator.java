package org.moddedmite.notenoughanimations;

import net.minecraft.Entity;
import net.minecraft.EntityBoat;
import net.minecraft.EntityHorse;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumItemInUseAction;
import net.minecraft.ItemStack;
import net.minecraft.ItemMap;
import net.minecraft.ModelBiped;
import net.minecraft.ModelBase;
import net.minecraft.MathHelper;
import traben.entity_model_features.compat.ActiveParts;
import traben.entity_model_features.compat.PoseManager;
import traben.entity_model_features.model.RenderContext;

/**
 * The MITE equivalent of NEA's player action pass. MITE's ModelBiped builds
 * the native pose first; this bridge snapshots the action-owned parts so EMF
 * can restore them after evaluating Fresh Animations' CEM model.
 */
public final class ThirdPersonAnimator {

    /** Called by the EMF external-pose bridge after CEM animation is evaluated. */
    public static void apply(ModelBase base, Entity entity, float partialTick) {
        if (base instanceof ModelBiped model && entity instanceof EntityPlayer
                && !RenderContext.isFirstPersonHand()) {
            // CEM owns the base model. Restore only the exact body parts that
            // the NEA action pass currently owns, matching NEA-EMF Compat.
            PoseManager.restore(entity, model);
        }
    }

    private static void applyVanillaExtras(ModelBiped model, EntityPlayer player, float partialTick) {
        applyMovement(model, player, partialTick);
        if (!player.isUsingItem()) return;
        ItemStack stack = player.getItemInUse();
        if (stack == null) return;
        EnumItemInUseAction action = stack.getItemInUseAction(player);
        if (action == EnumItemInUseAction.EAT || action == EnumItemInUseAction.DRINK) applyEatDrink(model);
    }

    /** Called from ModelBiped's rotation-angle tail before EMF evaluates CEM. */
    public static void captureVanillaPose(ModelBiped model, Entity entity, float partialTick) {
        if (!(entity instanceof EntityPlayer player) || RenderContext.isFirstPersonHand()) return;
        ItemStack using = player.isUsingItem() ? player.getItemInUse() : null;
        // Legacy MITE represents an item without a use action with null.
        EnumItemInUseAction action = using == null ? null : using.getItemInUseAction(player);
        ActiveParts active = activeParts(player.isSprinting(), player.ridingEntity instanceof EntityBoat,
                player.ridingEntity instanceof EntityHorse horse && horse.isHorseSaddled(),
                isMapHeld(player), action);
        if (active.any()) applyVanillaExtras(model, player, partialTick);
        PoseManager.capture(entity, model, active);
    }

    static ActiveParts activeParts(boolean sprinting, boolean boat, boolean saddledHorse,
                                   boolean map, EnumItemInUseAction action) {
        // Match the installed NEA-EMF Compat 1.0.6 allowlist for actions this
        // port implements. Vanilla holding, swinging, bow/blocking, swimming
        // and climbing do not take ownership away from Fresh Animations.
        // That compatibility mod also releases ordinary action arms during
        // sprinting. The pack disables NEA horse-leg and ladder animations.
        if (sprinting) return new ActiveParts(false, false);
        boolean bothArms = boat || saddledHorse || map;
        boolean eating = action == EnumItemInUseAction.EAT || action == EnumItemInUseAction.DRINK;
        return new ActiveParts(bothArms, bothArms || eating);
    }

    private static boolean isMapHeld(EntityPlayer player) {
        ItemStack held = player.inventory.getCurrentItemStack();
        return held != null && held.getItem() instanceof ItemMap;
    }

    private static void applyMovement(ModelBiped model, EntityPlayer player, float partialTick) {
        float time = player.ticksExisted + partialTick;
        if (player.ridingEntity instanceof EntityBoat) {
            float row = MathHelper.sin(time * 0.35F);
            model.bipedRightArm.rotateAngleX += -0.55F + row * 0.65F;
            model.bipedLeftArm.rotateAngleX += -0.55F - row * 0.65F;
            model.bipedRightArm.rotateAngleY += 0.18F;
            model.bipedLeftArm.rotateAngleY -= 0.18F;
            return;
        }
        if (player.ridingEntity instanceof EntityHorse horse && horse.isHorseSaddled()) {
            model.bipedRightArm.rotateAngleX -= 0.9F;
            model.bipedLeftArm.rotateAngleX -= 0.9F;
            model.bipedRightArm.rotateAngleY -= 0.12F;
            model.bipedLeftArm.rotateAngleY += 0.12F;
            return;
        }
        ItemStack held = player.inventory.getCurrentItemStack();
        if (held != null && held.getItem() instanceof ItemMap) {
            model.bipedRightArm.rotateAngleX = model.bipedRightArm.rotateAngleX * 0.35F - 0.85F;
            model.bipedLeftArm.rotateAngleX = model.bipedLeftArm.rotateAngleX * 0.35F - 0.85F;
            model.bipedRightArm.rotateAngleY -= 0.25F;
            model.bipedLeftArm.rotateAngleY += 0.25F;
        }
    }

    private static void applyEatDrink(ModelBiped model) {
        model.bipedRightArm.rotateAngleX = model.bipedRightArm.rotateAngleX * 0.5F - 1.15F;
        model.bipedRightArm.rotateAngleY = model.bipedHead.rotateAngleY - 0.2F;
    }
}
