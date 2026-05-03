package kr.pyke.blockhider.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kr.pyke.blockhider.BlockHider;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class BlockHiderSavedData extends SavedData {
    private static final Identifier FILE_NAME = BlockHider.id("blockhider_data");
    private static final float DEFAULT_ROTATION = 0.f;

    private final List<UUID> admins;
    private ResourceKey<Level> spawnDim;
    private Vec3 spawnPos;
    private float spawnYaw;
    private float spawnPitch;

    public static final Codec<BlockHiderSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.listOf().optionalFieldOf("admins", List.of()).forGetter(data -> data.admins),
        Level.RESOURCE_KEY_CODEC.optionalFieldOf("spawn_dimension").forGetter(data -> Optional.ofNullable(data.spawnDim)),
        Vec3.CODEC.optionalFieldOf("spawn_position").forGetter(data -> Optional.ofNullable(data.spawnPos)),
        Codec.FLOAT.optionalFieldOf("spawn_yaw", DEFAULT_ROTATION).forGetter(data -> data.spawnYaw),
        Codec.FLOAT.optionalFieldOf("spawn_pitch", DEFAULT_ROTATION).forGetter(data -> data.spawnPitch)
    ).apply(instance, BlockHiderSavedData::new));

    public static final SavedDataType<BlockHiderSavedData> TYPE = new SavedDataType<>(FILE_NAME, BlockHiderSavedData::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    public BlockHiderSavedData() {
        this.admins = new ArrayList<>();
        this.spawnYaw = DEFAULT_ROTATION;
        this.spawnPitch = DEFAULT_ROTATION;
    }

    private BlockHiderSavedData(List<UUID> admins, Optional<ResourceKey<Level>> spawnDim, Optional<Vec3> spawnPos, float spawnYaw, float spawnPitch) {
        this.admins = new ArrayList<>(admins);
        this.spawnDim = spawnDim.orElse(null);
        this.spawnPos = spawnPos.orElse(null);
        this.spawnYaw = spawnYaw;
        this.spawnPitch = spawnPitch;
    }

    public static BlockHiderSavedData get(MinecraftServer server) { return server.overworld().getDataStorage().computeIfAbsent(TYPE); }

    public boolean isAdmin(UUID uuid) { return admins.contains(uuid); }

    public List<UUID> getAdmins() { return Collections.unmodifiableList(admins); }

    public boolean addAdmin(UUID uuid) {
        if (admins.contains(uuid)) { return false; }

        admins.add(uuid);
        setDirty();

        return true;
    }

    public boolean removeAdmin(UUID uuid) {
        boolean removed = admins.remove(uuid);
        if (removed) { setDirty(); }

        return removed;
    }

    public ResourceKey<Level> getSpawnDimension() { return spawnDim; }

    public Vec3 getSpawnPosition() { return spawnPos; }

    public float getSpawnYaw() { return spawnYaw; }

    public float getSpawnPitch() { return spawnPitch; }

    public boolean hasSpawn() { return spawnDim != null && spawnPos != null; }

    public void setSpawn(ResourceKey<Level> dimension, Vec3 position, float yaw, float pitch) {
        this.spawnDim = dimension;
        this.spawnPos = position;
        this.spawnYaw = yaw;
        this.spawnPitch = pitch;
        setDirty();
    }

    public TeleportTransition createSpawnTransition(MinecraftServer server, TeleportTransition.PostTeleportTransition postTransition) {
        if (!hasSpawn()) { return null; }

        ServerLevel targetLevel = server.getLevel(spawnDim);
        if (targetLevel == null) { return null; }

        return new TeleportTransition(targetLevel, spawnPos, Vec3.ZERO, spawnYaw, spawnPitch, postTransition);
    }
}
