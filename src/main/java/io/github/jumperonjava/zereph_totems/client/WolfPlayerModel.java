package io.github.jumperonjava.zereph_totems.client;

import net.minecraft.client.model.*;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;

public class WolfPlayerModel extends PlayerEntityModel<AbstractClientPlayerEntity> {

    public final ModelPart furryEars;

    public WolfPlayerModel(ModelPart root, boolean thinArms) {
        super(root, thinArms);
        this.furryEars = root.getChild("head").getChild("furryEars");
    }
    public static TexturedModelData getModelData(Dilation dilation){
        ModelData modelData = PlayerEntityModel.getTexturedModelData(dilation,false);
        ModelPartData head = modelData.getRoot().getChild("head");
        var part = new ModelPartBuilder()
                .uv(24,0).cuboid(0,0,0,1,2,1)
                .uv(28,0).cuboid(1,0,1,1,2,1)
                .uv(32,0).cuboid(2,0,0,1,2,1)
                .uv(24,3).cuboid(0,-1,0,2,1,1)
                .uv(24,5).cuboid(-2,0,0,1,2,1)
                .uv(28,5).cuboid(-3,0,1,1,2,1)
                .uv(32,5).cuboid(-4,0,0,1,2,1)
                .uv(30,3).cuboid(-3,-1,0,2,1,1);

        head.addChild("furryEars",part, ModelTransform.of(0,0,0,0,0,0));

        return TexturedModelData.of(modelData,64,64);
    }
}
