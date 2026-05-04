package kr.pyke.blockhider.network.payload.s2c;

import kr.pyke.blockhider.BlockHider;
import kr.pyke.blockhider.transform.PlayerTransform;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.UUID;

public record S2C_TransformPayload(UUID playerUUID, Optional<BlockState> block, Optional<BlockPos> pos) implements CustomPacketPayload {
    public static final Type<S2C_TransformPayload> ID = new Type<>(BlockHider.id("s2c_transform"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2C_TransformPayload> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, S2C_TransformPayload::playerUUID,
        ByteBufCodecs.optional(ByteBufCodecs.fromCodec(BlockState.CODEC)), S2C_TransformPayload::block,
        ByteBufCodecs.optional(BlockPos.STREAM_CODEC), S2C_TransformPayload::pos,
        S2C_TransformPayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(S2C_TransformPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            var level = context.client().level;
            if (level == null) { return; }

            Player player = level.getPlayerByUUID(payload.playerUUID);
            if (player == null) { return; }

            ((PlayerTransform)player).blockhider$setTransformedBlock(payload.block.orElse(null), payload.pos.orElse(null));
        });
    }
}