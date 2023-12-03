package io.github.jumperonjava.zereph_totems.client;

import io.github.jumperonjava.zereph_totems.Furry;
import io.github.jumperonjava.zereph_totems.FurryState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.Set;

class EarsFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public static ModelPart ear = new ModelPart(List.of(), Map.of());
    public static ModelPart ear_skin = new ModelPart(List.of(),Map.of());
    private final WolfPlayerModel wolf;

    public EarsFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context, EntityRendererFactory.Context context1) {
        super(context);
        this.wolf = new WolfPlayerModel(context1.getPart(ZerephTotemsClient.FurryEarLayer),false);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, AbstractClientPlayerEntity player, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (((Furry) player).getFurryState() == FurryState.HUMAN) return;
        wolf.furryEars.setPivot(0.5f, -10, -3);
        getContextModel().copyBipedStateTo(wolf);
        getContextModel().getHead().rotate(matrixStack);
        int m = LivingEntityRenderer.getOverlay(player, 0.0F);
        VertexConsumer vertexConsumer;
        vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(new Identifier("zereph_totem", "textures/ears.png")));
        wolf.furryEars.render(matrixStack, vertexConsumer, light, m, 1, 1, 1, 1);
        if (((Furry) player).isCustomTexture()) {
            vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(player.getSkinTexture()));
        }
        wolf.furryEars.render(matrixStack, vertexConsumer, light, m, 1, 1, 1, 1);
    }
}