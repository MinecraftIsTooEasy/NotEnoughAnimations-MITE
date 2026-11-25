package org.moddedmite.notenoughanimations;

import net.fabricmc.api.ClientModInitializer;
import traben.entity_model_features.model.RenderContext;

/** MITE client bootstrap for the third-person action layer. */
public final class NotEnoughAnimationsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        RenderContext.registerExternalPoseHook(ThirdPersonAnimator::apply);
    }
}
