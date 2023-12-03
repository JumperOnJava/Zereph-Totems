package io.github.jumperonjava.zereph_totems.mixin;

import io.github.jumperonjava.zereph_totems.TotemEffects;
import io.github.jumperonjava.zereph_totems.TotemEvents;
import io.github.jumperonjava.zereph_totems.ZerephTotems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MilkBucketItem.class)
public abstract class MIlkBucketItemMixin {
    @Shadow public abstract TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand);

    @Inject(method = "finishUsing",at = @At(value = "INVOKE",target = "Lnet/minecraft/entity/LivingEntity;clearStatusEffects()Z",shift = At.Shift.BEFORE))
    void onClearEffects(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir){
        TotemEvents.onMilkUse(stack,world,user);
    }
}
