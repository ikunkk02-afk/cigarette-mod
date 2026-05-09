package com.example.examplemod;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TreatmentHudSyncPayload(
        boolean diagnosedLungCancer,
        int treatmentStage,
        int treatmentProgress,
        int smokeFreeTicks,
        int treatmentCooldown) implements CustomPacketPayload {

    public static final Type<TreatmentHudSyncPayload> TYPE = new Type<>(cigaretteMod.id("treatment_hud_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TreatmentHudSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public TreatmentHudSyncPayload decode(RegistryFriendlyByteBuf buffer) {
            return new TreatmentHudSyncPayload(
                    buffer.readBoolean(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, TreatmentHudSyncPayload payload) {
            buffer.writeBoolean(payload.diagnosedLungCancer());
            buffer.writeVarInt(payload.treatmentStage());
            buffer.writeVarInt(payload.treatmentProgress());
            buffer.writeVarInt(payload.smokeFreeTicks());
            buffer.writeVarInt(payload.treatmentCooldown());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TreatmentHudSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientTreatmentData.apply(payload));
    }

    public static void syncTo(ServerPlayer player) {
        PlayerSmokingData data = PlayerSmokingData.get(player);
        PacketDistributor.sendToPlayer(player, new TreatmentHudSyncPayload(
                data.diagnosedLungCancer(),
                data.treatmentStage(),
                data.treatmentProgress(),
                data.smokeFreeTicks(),
                data.treatmentCooldown()));
    }
}
