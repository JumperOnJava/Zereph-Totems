package io.github.jumperonjava.zereph_totems;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static io.github.jumperonjava.zereph_totems.ZerephTotems.MODID;

public class TotemEffects {
    public static final StatusEffect LOW_FLAMES = new StatusEffect(StatusEffectCategory.HARMFUL,0xFFfc7f03){};
    public TotemEffects(){
        Registry.register(Registries.STATUS_EFFECT,new Identifier(MODID,"low_flames"),LOW_FLAMES);
    }
}
