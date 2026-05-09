package com.example.examplemod;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AddictionHudSyncPayload(
        int smokedCigaretteCount,
        int addictionStage,
        boolean hasCoughing,
        boolean hasLungCancer,
        boolean isSmoking) implements CustomPacketPayload {
    public static final Type<AddictionHudSyncPayload> TYPE = new Type<>(cigaretteMod.id("addiction_hud_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AddictionHudSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public AddictionHudSyncPayload decode(RegistryFriendlyByteBuf buffer) {
            return new AddictionHudSyncPayload(
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, AddictionHudSyncPayload payload) {
            buffer.writeVarInt(payload.smokedCigaretteCount());
            buffer.writeVarInt(payload.addictionStage());
            buffer.writeBoolean(payload.hasCoughing());
            buffer.writeBoolean(payload.hasLungCancer());
            buffer.writeBoolean(payload.isSmoking());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AddictionHudSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientAddictionHudData.apply(payload));
    }
}
