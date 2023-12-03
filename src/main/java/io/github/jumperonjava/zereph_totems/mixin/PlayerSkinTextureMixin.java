package io.github.jumperonjava.zereph_totems.mixin;

import io.github.jumperonjava.zereph_totems.client.SkinGetter;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.PlayerSkinTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerSkinTexture.class)
public class PlayerSkinTextureMixin implements SkinGetter {
    public NativeImage getSavedTexture() {
        return savedTexture;
    }

    @Unique
    private NativeImage savedTexture;

    @Inject(method = "uploadTexture",at = @At("HEAD"))
    void saveTexture(NativeImage image, CallbackInfo ci){
        this.savedTexture = image;
    }
}
