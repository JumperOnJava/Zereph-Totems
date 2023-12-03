package io.github.jumperonjava.zereph_totems.mixin;

import io.github.jumperonjava.zereph_totems.Furry;
import io.github.jumperonjava.zereph_totems.FurryState;
import io.github.jumperonjava.zereph_totems.PacketManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class PlayerMixin extends LivingEntity implements Furry {

    private boolean customEar;

    @Shadow public abstract ItemStack eatFood(World world, ItemStack stack);

    @Shadow @Final private static Logger LOGGER;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public FurryState getFurryState() {
        return furryState;
    }
    private static UUID auuid1 = new UUID(0x4A617661,0x4A6D7071);
    private static UUID auuid2 = new UUID(0x4A617662,0x4A6D7072);
    private static UUID auuid3 = new UUID(0x4A617663,0x4A6D7073);
    private static UUID auuid4 = new UUID(0x4A617664,0x4A6D7074);
    @Override
    public void setFurryState(FurryState furryState) {
        try {
            this.furryState = furryState;
            this.getAttributes()
                    .getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH)
                    .removeModifier(auuid1);
            this.getAttributes()
                    .getCustomInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                    .removeModifier(auuid2);
            this.getAttributes()
                    .getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                    .removeModifier(auuid3);
            this.getAttributes()
                    .getCustomInstance(EntityAttributes.GENERIC_ATTACK_SPEED)
                    .removeModifier(auuid4);
            if (furryState == FurryState.FURRY) {
                this.getAttributes()
                        .getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH)
                        .addTemporaryModifier(new EntityAttributeModifier(auuid1, "furry_health", -4, EntityAttributeModifier.Operation.ADDITION));
                this.getAttributes()
                        .getCustomInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                        .addTemporaryModifier(new EntityAttributeModifier(auuid2, "furry_damage", 4, EntityAttributeModifier.Operation.ADDITION));
                this.getAttributes()
                        .getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                        .addTemporaryModifier(new EntityAttributeModifier(auuid3, "furry_movement", -0.2, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                this.getAttributes()
                        .getCustomInstance(EntityAttributes.GENERIC_ATTACK_SPEED)
                        .addTemporaryModifier(new EntityAttributeModifier(auuid4, "furry_attack", 0.15, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                setHealth(Math.min(getHealth(),16));
            }
            if((Object)this instanceof ServerPlayerEntity){
                PacketManager.Server.sendFurryStateAround((ServerPlayerEntity)(Object)this);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void setCustomTexture(boolean isCustom) {
        this.customEar = isCustom;
        if((Object)this instanceof ServerPlayerEntity){
            PacketManager.Server.sendFurryStateAround((ServerPlayerEntity)(Object)this);
        }
    }

    @Override
    public boolean isCustomTexture() {
        return this.customEar;
    }

    @Unique
    private FurryState furryState = FurryState.HUMAN;

    @Inject(method = "writeCustomDataToNbt",at = @At("RETURN"))
    public void addFurryNbt(NbtCompound nbt, CallbackInfo ci){
        nbt.putByte("FurryState", (byte) getFurryState().ordinal());
        nbt.putBoolean("FurryCustom", isCustomTexture());
    }
    @Inject(method = "readCustomDataFromNbt",at = @At("RETURN"))
    public void readFurryNbt(NbtCompound nbt, CallbackInfo ci){
        setFurryState(FurryState.values()[(nbt.getByte("FurryState"))]);
        setCustomTexture(nbt.getBoolean("FurryCustom"));
    }
}
