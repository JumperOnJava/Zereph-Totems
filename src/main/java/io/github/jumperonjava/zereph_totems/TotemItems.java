package io.github.jumperonjava.zereph_totems;

import io.github.jumperonjava.zereph_totems.ZerephTotems;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.RegistryWorldView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.slf4j.LoggerFactory;

public class TotemItems {
    public static final Item WOLF_TOTEM = new Item(new FabricItemSettings().maxDamage(4).maxDamageIfAbsent(4)){
        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            if (!(user instanceof ServerPlayerEntity serverPlayer)) {
                return TypedActionResult.fail(user.getStackInHand(hand));
            }
            var stack = user.getStackInHand(hand);
            stack.damage(1, serverPlayer, (p) -> p.sendToolBreakStatus(hand));
            var fuser = ((Furry) user);
            if (fuser.getFurryState() == FurryState.FURRY) {
                fuser.setFurryState(FurryState.HUMAN);
            } else if (fuser.getFurryState() == FurryState.HUMAN) {
                fuser.setFurryState(FurryState.FURRY);
            }
            user.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED,5f,1f);
            return TypedActionResult.success(user.getStackInHand(hand));
        }
    };
    public static final Item PHOENIX_TOTEM = new Item(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON));
    public static final Item WILD_TOTEM = new Item(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON));
    public TotemItems(){
        Registry.register(Registries.ITEM,new Identifier(ZerephTotems.MODID,"wolf_totem"),WOLF_TOTEM);
        Registry.register(Registries.ITEM,new Identifier(ZerephTotems.MODID,"phoenix_totem"),PHOENIX_TOTEM);
        Registry.register(Registries.ITEM,new Identifier(ZerephTotems.MODID,"wild_totem"),WILD_TOTEM);
    }
}
