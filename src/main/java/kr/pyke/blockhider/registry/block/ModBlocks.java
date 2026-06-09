package kr.pyke.blockhider.registry.block;

import kr.pyke.blockhider.BlockHider;
import kr.pyke.blockhider.registry.block.ghost.GhostBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Function;

public class ModBlocks {
    public static final Block GHOST_BLOCK = register("ghost_block", GhostBlock::new, BlockBehaviour.Properties.of()
        .strength(-1.f, 3600000.f)
        .mapColor(MapColor.NONE)
        .noLootTable()
        .noOcclusion()
        .isValidSpawn(Blocks::never)
        .noTerrainParticles()
        .pushReaction(PushReaction.BLOCK)
    );

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        ResourceKey<Block> resourceKey = ResourceKey.create(Registries.BLOCK, BlockHider.id(name));

        return Registry.register(BuiltInRegistries.BLOCK, resourceKey, factory.apply(properties.setId(resourceKey)));
    }

    public static void register() { }

    private ModBlocks() { }
}