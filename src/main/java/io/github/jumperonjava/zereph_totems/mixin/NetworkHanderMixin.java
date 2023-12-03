package io.github.jumperonjava.zereph_totems.mixin;

import io.github.jumperonjava.zereph_totems.TotemItems;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ClientPlayNetworkHandler.class)

public class NetworkHanderMixin {
    @Redirect(method = "getActiveTotemOfUndying",at = @At(value = "INVOKE",target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    private static boolean isOfWild(ItemStack instance, Item item)
    {
        return instance.isOf(item) || instance.isOf(TotemItems.WILD_TOTEM);
    }

    @Inject(method = "getActiveTotemOfUndying",at = @At("TAIL"),cancellable = true)
    private static void changeToPhoenixTotem(PlayerEntity player, CallbackInfoReturnable<ItemStack> cir){
        var inv = player.getInventory();
        var lists = List.of(inv.main,inv.offHand);
        for(var list : lists)
            for(var slot : list){
                if(slot.isOf(TotemItems.PHOENIX_TOTEM)){
                    cir.setReturnValue(slot);
                }
            }
    }
}
