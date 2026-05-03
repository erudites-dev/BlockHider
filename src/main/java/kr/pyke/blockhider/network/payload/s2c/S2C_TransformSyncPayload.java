package kr.pyke.blockhider.network.payload.s2c;

import kr.pyke.blockhider.BlockHider;
import kr.pyke.blockhider.transform.PlayerTransform;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record S2C_TransformSyncPayload(Map<UUID, BlockState> entries) implements CustomPacketPayload {
    public static final Type<S2C_TransformSyncPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(BlockHider.MOD_ID, "s2c_transform_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2C_TransformSyncPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, ByteBufCodecs.fromCodec(BlockState.CODEC)), S2C_TransformSyncPayload::entries,
        S2C_TransformSyncPayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(S2C_TransformSyncPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            var level = context.client().level;
            if (level == null) { return; }

            for (Player player : level.players()) {
                ((PlayerTransform) player).blockhider$setTransformedBlock(null);
            }
            for (Map.Entry<UUID, BlockState> entry : payload.entries.entrySet()) {
                Player player = level.getPlayerByUUID(entry.getKey());
                if (player != null) {
                    ((PlayerTransform) player).blockhider$setTransformedBlock(entry.getValue());
                }
            }
        });
    }
}