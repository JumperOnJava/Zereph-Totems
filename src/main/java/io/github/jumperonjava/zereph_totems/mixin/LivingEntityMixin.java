package io.github.jumperonjava.zereph_totems.mixin;

import io.github.jumperonjava.zereph_totems.Furry;
import io.github.jumperonjava.zereph_totems.FurryState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Redirect(method = "applyFoodEffects",at = @At(value = "INVOKE",target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z"))
    boolean onHunger(LivingEntity instance, StatusEffectInstance effect){
        if(effect.getEffectType() == StatusEffects.HUNGER)
            if(instance instanceof ServerPlayerEntity serverPlayer){
                if(((Furry)serverPlayer).getFurryState() == FurryState.FURRY){
                    return false;
                }
            }
        instance.addStatusEffect(effect);
        return false;
    }
}
