package io.github.jumperonjava.zereph_totems;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.TypeFilter;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import static net.minecraft.server.command.CommandManager.literal;
public class TotemEvents {
    private static boolean debug=FabricLoader.getInstance().isDevelopmentEnvironment();

    public TotemEvents(){
        ServerLivingEntityEvents.ALLOW_DEATH.register(this::allowDeath);
        ServerLivingEntityEvents.AFTER_DEATH.register(this::onEntityKill);
        CommandRegistrationCallback.EVENT.register(this::registerWildDebug);
        ServerPlayerEvents.AFTER_RESPAWN.register(this::copyFurryState);
        CommandRegistrationCallback.EVENT.register(this::registerCustomTexture);
    }

    public static void onMilkUse(ItemStack stack, World world, LivingEntity user) {
        try {
            if (user.hasStatusEffect(TotemEffects.LOW_FLAMES)) {
                user.getWorld().createExplosion(
                        null,
                        user.getX(),
                        user.getY(),
                        user.getZ(),
                        4 * (user.getStatusEffect(TotemEffects.LOW_FLAMES).getDuration() / 18000f),
                        World.ExplosionSourceType.MOB
                );
            }
        }
        catch (Exception e){
            if(debug)
                user.sendMessage(Text.literal(e.getMessage()));
        }
    }

    private void onEntityKill(LivingEntity deadEntity,DamageSource source) {
        var mp = 1f;
        if(source != null && source.getAttacker() instanceof LivingEntity entity2)
            mp += EnchantmentHelper.getLooting(entity2);
        if(deadEntity instanceof BlazeEntity)
            if(deadEntity.getRandom().nextInt(400)<=mp)
                deadEntity.getWorld().spawnEntity(new ItemEntity(deadEntity.getWorld(),deadEntity.getX(),deadEntity.getY(),deadEntity.getZ(),new ItemStack(TotemItems.PHOENIX_TOTEM)));
        if(deadEntity instanceof PassiveEntity)
            if(deadEntity.getRandom().nextInt(300)<=mp)
                deadEntity.getWorld().spawnEntity(new ItemEntity(deadEntity.getWorld(),deadEntity.getX(),deadEntity.getY(),deadEntity.getZ(),new ItemStack(TotemItems.WILD_TOTEM)));
    }

    private void registerCustomTexture(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(literal("toggleeartexture").executes(context -> {
            var fpl = ((Furry)context.getSource().getPlayer());
            var iscustom = fpl.isCustomTexture();
            fpl.setCustomTexture(!iscustom);
            return 1;
        }));
    }

    private void copyFurryState(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean b) {
        ((Furry)newPlayer).setFurryState(((Furry)oldPlayer).getFurryState());
        ((Furry)newPlayer).setCustomTexture(((Furry)oldPlayer).isCustomTexture());
    }

    private final List<WildTotemEvent> wildTotemEvents = List.of(
            new WildTotemEvent("chorusFruitTp", false, this::chorusFruitTp),
            new WildTotemEvent("damageNearby", true, this::damageNearby),
            new WildTotemEvent("witherNearby", true, this::witherNearby),
            new WildTotemEvent("spawnWarp", false, this::spawnWarp),
            new WildTotemEvent("powerplay", false, this::powerplay),
            new WildTotemEvent("arrowRing", true, this::arrowRing)
    );

    private void toggleDebug(LivingEntity entity) {
        this.debug = !debug;
        entity.sendMessage(Text.of("debug mode is %s".formatted(debug?"on":"off")));
    }

    private void registerWildDebug(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        var command = literal("wilddebug");
        var l = new ArrayList<>(wildTotemEvents);
        l.add(new WildTotemEvent("random",true,this::triggerRandomWildEffect));
        l.add(new WildTotemEvent("toggleDebug", true, this::toggleDebug));
        for(int i=0;i<l.size();i++){
            var i2 = new int[]{i};
            command.then(literal(l.get(i).eventName).executes((context)->
            {
                try {
                    l.get(i2[0]).eventHandler.accept((LivingEntity) context.getSource().getEntity());
                }
                catch (Exception e){
                    e.printStackTrace();
                    context.getSource().getPlayer().sendMessage(Text.literal(e.getMessage()).append(Arrays.toString(e.getStackTrace())));
                }
                return 1;
            }));
        }
        dispatcher.register(command);
    }

    private boolean allowDeath(LivingEntity livingEntity, DamageSource damageSource, float v) {
        if(!(livingEntity instanceof PlayerEntity player))
            return true;
        var inv = player.getInventory();
        var lists = List.of(inv.main,inv.offHand);
        for(var hand : player.getHandItems()){
            if(hand.isOf(TotemItems.WILD_TOTEM)){
                hand.decrement(1);
                player.clearStatusEffects();
                player.setHealth(1.0f);
                triggerRandomWildEffect(livingEntity);
                player.getWorld().sendEntityStatus(player, (byte)35);
                return false;
            }
        }
        if(player.hasStatusEffect(TotemEffects.LOW_FLAMES))
            return true;
        for(var list : lists)
            for(var slot : list){
                if(slot.isOf(TotemItems.PHOENIX_TOTEM)){
                    if (player instanceof ServerPlayerEntity player1) {
                        ServerPlayerEntity serverPlayerEntity = player1;
                        serverPlayerEntity.incrementStat(Stats.USED.getOrCreateStat(TotemItems.PHOENIX_TOTEM));
                        Criteria.USED_TOTEM.trigger(serverPlayerEntity, slot);
                    }

                    player.setHealth(player.getMaxHealth());
                    player.clearStatusEffects();
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));
                    player.addStatusEffect(new StatusEffectInstance(TotemEffects.LOW_FLAMES, 18000, 0));
                    player.getWorld().sendEntityStatus(player, (byte)35);
                    return false;
                }
            }
        return true;
    }

    private void triggerRandomWildEffect(LivingEntity livingEntity) {
        var l = new ArrayList<>(wildTotemEvents);
        debugLog(livingEntity,"found %d targets".formatted(getTargetsOf(livingEntity).size()));
        if(getTargetsOf(livingEntity).isEmpty()){
            l.removeIf(e->e.isTarget);
            debugLog(livingEntity,"removed mob targeted events");
        }
        var r = new Random().nextInt(l.size());
        l.get(r).eventHandler.accept(livingEntity);
        debugLog(livingEntity,l.get(r).eventName);
    }

    private void arrowRing(LivingEntity livingEntity) {
        debugLog(livingEntity,"created arrows for nearby %d mobs".formatted(getTargetsOf(livingEntity).size()));
        var ppos = livingEntity.getPos();
        for(var target : getTargetsOf(livingEntity)){
            var arrow = new ArrowEntity(livingEntity.getWorld(),0,0,0);
            arrow.setPosition(ppos.add(0,livingEntity.getHeight()/2,0));
            var power = 4;
            var vec = target.getPos().add(0,target.getHeight()/2,0).add(arrow.getPos().multiply(-1)).normalize().multiply(power);
            arrow.setVelocity(vec);
            arrow.setPierceLevel((byte) 127);
            arrow.life = 1000;
            livingEntity.getWorld().spawnEntity(arrow);
            debugLog(livingEntity,"creating arrow for ",(target.getName()));
        }
        addParticles(livingEntity,ParticleTypes.FIREWORK);
    }

    private void powerplay(LivingEntity livingEntity) {
        livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION,5*20,4));
        livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE,5*20));
        livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH,5*20,2));
        addParticles(livingEntity,ParticleTypes.FLAME);
    }

    private void spawnWarp(LivingEntity livingEntity) {
        if(!(livingEntity instanceof ServerPlayerEntity player))
            return;
        var dim = player.getSpawnPointDimension();
        var pos = player.getSpawnPointPosition();
        if(pos == null)
            pos = player.getServer().getOverworld().getSpawnPos();
        player.teleport(player.getServerWorld().getServer().getWorld(dim),pos.getX(),pos.getY(),pos.getZ(),player.getYaw(),player.getPitch());
        addParticles(livingEntity,ParticleTypes.HAPPY_VILLAGER);
    }

    private void witherNearby(LivingEntity livingEntity) {
        debugLog(livingEntity,"withered nearby %d mobs".formatted(getTargetsOf(livingEntity).size()));
        var targets = getTargetsOf(livingEntity);
        targets.forEach(t->{
            t.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER,20*10),livingEntity);
            debugLog(livingEntity,"withered ",t.getName());
        });
        addParticles(livingEntity,ParticleTypes.SMOKE);
    }
    private void addParticles(LivingEntity player, ParticleEffect particle){
        var pos = player.getPos();
        var world = player.getWorld();
        for(float i=0;i<2*Math.PI;i+=Math.PI/24){
            ((ServerChunkManager)world.getChunkManager()).sendToNearbyPlayers(
                    player,
                    new ParticleS2CPacket(
                            particle,
                            true,
                            pos.x + Math.cos(i),
                            new Random().nextFloat((float) pos.y, (float) (pos.y+player.getHeight()/2)),
                            pos.z + Math.sin(i),
                            0,
                            0,
                            0,
                            1,
                            3));
        }
    }

    private List<LivingEntity> getTargetsOf(LivingEntity livingEntity) {
        var damagebox = livingEntity.getBoundingBox().expand(10);
        damagebox.expand(10, 3, 10);
        damagebox.withMaxY(damagebox.maxY+4);
        var target = new ArrayList<LivingEntity>();
        target.addAll(livingEntity.getWorld().getEntitiesByType(TypeFilter.instanceOf(HostileEntity.class), damagebox, (e) -> true));
        target.addAll(livingEntity.getWorld().getEntitiesByType(TypeFilter.instanceOf(GolemEntity.class), damagebox, (e) -> true));
        target.addAll(livingEntity.getWorld().getEntitiesByType(TypeFilter.instanceOf(SlimeEntity.class), damagebox, (e) -> true));
        target.addAll(livingEntity.getWorld().getEntitiesByType(TypeFilter.instanceOf(FlyingEntity.class), damagebox, (e) -> true));
        target.addAll(livingEntity.getWorld().getEntitiesByType(TypeFilter.instanceOf(PlayerEntity.class), damagebox, (e) -> e != livingEntity));
        return target;
    }

    private void damageNearby(LivingEntity livingEntity) {
        debugLog(livingEntity,"damaged nearby %d mobs".formatted(getTargetsOf(livingEntity).size()));
        var targets = getTargetsOf(livingEntity);
        targets.forEach(t-> {
            debugLog(livingEntity);
            t.damage(livingEntity.getDamageSources().magic(),6.0f);
            debugLog(livingEntity,"damaged ", t.getName());
        });
        livingEntity.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,1,1);
        addParticles(livingEntity,ParticleTypes.CRIT);
    }

    private void chorusFruitTp(LivingEntity livingEntity) {
        Items.CHORUS_FRUIT.finishUsing(ItemStack.EMPTY,livingEntity.getWorld(),livingEntity);
        livingEntity.playSound(SoundEvents.BLOCK_CHORUS_FLOWER_DEATH,1,1);
        addParticles(livingEntity,ParticleTypes.END_ROD);
    }

    public void debugLog(LivingEntity entity, Object... info){
        MutableText s = Text.literal("");
        for(var i : info){
            if(i instanceof MutableText)
                s = s.append((Text) i);
            else
                s = s.append(String.valueOf(i));
        }
        if(debug)
            entity.sendMessage(s);
    }

    public record WildTotemEvent(String eventName, boolean isTarget, Consumer<LivingEntity> eventHandler) {
    }

}
