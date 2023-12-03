package io.github.jumperonjava.zereph_totems;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;


public class PacketManager {
    public static final Identifier CHANNEL = new Identifier("zereph_totems","communication");
    public static class Client{
        public Client(){
            ClientPlayNetworking.registerGlobalReceiver(EntityStatusPacket.ENTITY_STATUS_PACKET_TYPE,this::onStatusPacket);
        }

        private void onStatusPacket(EntityStatusPacket entityStatusPacket, ClientPlayerEntity clientPlayerEntity, PacketSender packetSender) {
            Entity entity = clientPlayerEntity.getWorld().getEntityById(entityStatusPacket.entity);
            if (entity != null) {
                try{
                    var e = entity;
                    var fe = (Furry)e;
                    fe.setFurryState(FurryState.values()[entityStatusPacket.status&1]);
                    fe.setCustomTexture((entityStatusPacket.status & 2) >> 1 == 1);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

    }
    public static class Server{
        public Server(){
            ServerPlayConnectionEvents.JOIN.register(this::onPlayerJoin);
            ServerTickEvents.END_SERVER_TICK.register(this::tickFurryUpdate);
        }
        private int updatecount=0;
        private void tickFurryUpdate(MinecraftServer minecraftServer) {
            updatecount=(updatecount+1)%100;
            if(updatecount==0){
                minecraftServer.getPlayerManager().getPlayerList().forEach(Server::sendFurryStateAround);
            }
        }

        public static void sendFurryStateAround(ServerPlayerEntity serverPlayerEntity) {
            serverPlayerEntity.getServer().getPlayerManager().getPlayerList().forEach(p->{
                var fplayer = ((Furry)serverPlayerEntity);
                int state = 0;
                state = state | fplayer.getFurryState().ordinal();
                state = state | ((fplayer.isCustomTexture()?1:0)<<1);
                ServerPlayNetworking.send(p,new EntityStatusPacket(serverPlayerEntity,state));
            });
        }

        private void onPlayerJoin(ServerPlayNetworkHandler serverPlayNetworkHandler, PacketSender packetSender, MinecraftServer minecraftServer) {
            sendFurryStateAround(serverPlayNetworkHandler.player);
        }

    }
    public record EntityStatusPacket(int entity, int status) implements FabricPacket{
        public EntityStatusPacket(Entity entity, int status) {
            this(entity.getId(),status);
        }
        public EntityStatusPacket(PacketByteBuf buf) {
            this(buf.readInt(),buf.readInt());
        }

        @Override
        public void write(PacketByteBuf buf) {
            buf.writeInt(entity);
            buf.writeInt(status);
        }

        @Override
        public PacketType<?> getType() {
            return ENTITY_STATUS_PACKET_TYPE;
        }
        public static final PacketType<EntityStatusPacket> ENTITY_STATUS_PACKET_TYPE = PacketType.create(CHANNEL, EntityStatusPacket::new);
    }
}
