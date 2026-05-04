package kr.pyke.blockhider.network.payload.s2c;

import io.netty.buffer.ByteBuf;
import kr.pyke.blockhider.BlockHider;
import kr.pyke.blockhider.client.state.ClientGameState;
import kr.pyke.blockhider.type.GAME_ROLE;
import kr.pyke.blockhider.type.GAME_STATE;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public record S2C_GameStatePayload(GAME_STATE state, int remainingSeconds, int totalSeconds, Optional<GAME_ROLE> role, int aliveSeekers, int aliveHiders, int hintUsed, int hintMax) implements CustomPacketPayload {
    public static final Type<S2C_GameStatePayload> ID = new Type<>(BlockHider.id("s2c_game_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2C_GameStatePayload> STREAM_CODEC = StreamCodec.of(S2C_GameStatePayload::encode, S2C_GameStatePayload::decode);

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    private static void encode(RegistryFriendlyByteBuf buf, S2C_GameStatePayload payload) {
        writeEnum(buf, payload.state);
        buf.writeVarInt(payload.remainingSeconds);
        buf.writeVarInt(payload.totalSeconds);

        if (payload.role.isPresent()) {
            buf.writeBoolean(true);
            writeEnum(buf, payload.role.get());
        }
        else {
            buf.writeBoolean(false);
        }

        buf.writeVarInt(payload.aliveSeekers);
        buf.writeVarInt(payload.aliveHiders);
        buf.writeVarInt(payload.hintUsed);
        buf.writeVarInt(payload.hintMax);
    }

    private static S2C_GameStatePayload decode(RegistryFriendlyByteBuf buf) {
        GAME_STATE state = readEnum(buf, GAME_STATE.class);
        int remaining = buf.readVarInt();
        int total = buf.readVarInt();
        Optional<GAME_ROLE> role = buf.readBoolean() ? Optional.of(readEnum(buf, GAME_ROLE.class)) : Optional.empty();
        int aliveSeekers = buf.readVarInt();
        int aliveHiders = buf.readVarInt();
        int hintUsed = buf.readVarInt();
        int hintMax = buf.readVarInt();
        return new S2C_GameStatePayload(state, remaining, total, role, aliveSeekers, aliveHiders, hintUsed, hintMax);
    }

    private static <E extends Enum<E>> void writeEnum(ByteBuf buf, E value) {
        buf.writeInt(value.ordinal());
    }

    private static <E extends Enum<E>> E readEnum(ByteBuf buf, Class<E> enumClass) {
        return enumClass.getEnumConstants()[buf.readInt()];
    }

    public static void handle(S2C_GameStatePayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> ClientGameState.update(payload.state, payload.remainingSeconds, payload.totalSeconds, payload.role, payload.aliveSeekers, payload.aliveHiders, payload.hintUsed, payload.hintMax));
    }
}
