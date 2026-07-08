package kr.pyke.blockhider.network.payload.s2c;

import kr.pyke.blockhider.BlockHider;
import kr.pyke.blockhider.client.state.ClientGameState;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

public record S2C_SeekerListPayload(String seekers) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2C_SeekerListPayload> ID = new CustomPacketPayload.Type<>(BlockHider.id("s2c_seeker_list"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2C_SeekerListPayload> STREAM_CODEC = StreamCodec.of(S2C_SeekerListPayload::encode, S2C_SeekerListPayload::decode);

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    private static void encode(RegistryFriendlyByteBuf buf, S2C_SeekerListPayload payload) {
        buf.writeUtf(payload.seekers);
    }

    private static S2C_SeekerListPayload decode(RegistryFriendlyByteBuf buf) {
        String seekers = buf.readUtf();

        return new S2C_SeekerListPayload(seekers);
    }

    public static void handle(S2C_SeekerListPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> ClientGameState.update(payload.seekers()));
    }
}
