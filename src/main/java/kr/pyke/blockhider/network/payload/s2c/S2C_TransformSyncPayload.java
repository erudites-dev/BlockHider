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
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record S2C_TransformSyncPayload(Map<UUID, TransformEntry> entries) implements CustomPacketPayload {
    public static final Type<S2C_TransformSyncPayload> ID = new Type<>(BlockHider.id("s2c_transform_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TransformEntry> ENTRY_CODEC = StreamCodec.composite(
        ByteBufCodecs.fromCodec(BlockState.CODEC), TransformEntry::block,
        BlockPos.STREAM_CODEC, TransformEntry::pos,
        TransformEntry::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, S2C_TransformSyncPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, ENTRY_CODEC), S2C_TransformSyncPayload::entries,
        S2C_TransformSyncPayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(S2C_TransformSyncPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            var level = context.client().level;
            if (level == null) { return; }

            Set<UUID> syncedUUIDs = payload.entries.keySet();
            for (Player player : level.players()) {
                if (!syncedUUIDs.contains(player.getUUID())) {
                    ((PlayerTransform)player).blockhider$setTransformedBlock(null, null);
                }
            }
            for (Map.Entry<UUID, TransformEntry> entry : payload.entries.entrySet()) {
                Player player = level.getPlayerByUUID(entry.getKey());
                if (player == null) { continue; }

                ((PlayerTransform)player).blockhider$setTransformedBlock(entry.getValue().block(), entry.getValue().pos());
            }
        });
    }

    public record TransformEntry(BlockState block, BlockPos pos) { }
}