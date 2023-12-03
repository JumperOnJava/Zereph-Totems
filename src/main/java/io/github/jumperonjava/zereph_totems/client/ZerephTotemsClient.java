package io.github.jumperonjava.zereph_totems.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.jumperonjava.zereph_totems.Furry;
import io.github.jumperonjava.zereph_totems.FurryState;
import io.github.jumperonjava.zereph_totems.PacketManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ZerephTotemsClient implements ClientModInitializer {
    public static Identifier FurryEarId;
    public static EntityModelLayer FurryEarLayer = new EntityModelLayer(new Identifier("zereph_totem","furryears"),"furryears");

    @Override
    public void onInitializeClient() {
        new PacketManager.Client();
            HudRenderCallback.EVENT.register(this::writeisfurry);
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(this::onEntityRender);
        EntityModelLayerRegistry.registerModelLayer(FurryEarLayer,()-> WolfPlayerModel.getModelData(Dilation.NONE));
    }

    private void onEntityRender(EntityType<? extends LivingEntity> entityType, LivingEntityRenderer<?, ?> livingEntityRenderer, LivingEntityFeatureRendererRegistrationCallback.RegistrationHelper registrationHelper, EntityRendererFactory.Context context) {
        if (livingEntityRenderer instanceof PlayerEntityRenderer plrender) {
            registrationHelper.register(new EarsFeatureRenderer(plrender,context));
        }
    }

    private void writeisfurry(DrawContext context, float v) {
        if(MinecraftClient.getInstance().player==null)
            return;
        if(!FabricLoader.getInstance().isDevelopmentEnvironment())
            return;
        context.drawText(
                MinecraftClient.getInstance().textRenderer,
                Text.literal(String.valueOf(((Furry)MinecraftClient.getInstance().player).getFurryState())),
                10,
                10,
                0xFFFFFFFF,
                true
                );
    }
}
