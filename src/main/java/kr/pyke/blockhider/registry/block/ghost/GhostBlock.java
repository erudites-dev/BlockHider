package kr.pyke.blockhider.registry.block.ghost;

import com.mojang.serialization.MapCodec;
import kr.pyke.blockhider.game.GameManager;
import kr.pyke.blockhider.game.PlayerGameData;
import kr.pyke.blockhider.type.GAME_ROLE;
import kr.pyke.blockhider.type.GAME_STATE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class GhostBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<GhostBlock> CODEC = simpleCodec(GhostBlock::new);

    private static final VoxelShape SHAPE_NS = Block.box(4, 0, 0, 12, 16, 16);
    private static final VoxelShape SHAPE_EW = Block.box(0, 0, 4, 16, 16, 12);

    public GhostBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected @NotNull MapCodec<GhostBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @NotNull BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        Direction facing = state.getValue(FACING);

        return facing.getAxis() == Direction.Axis.X ? SHAPE_NS : SHAPE_EW;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        if (!(context instanceof EntityCollisionContext entityContext)) { return this.getShape(state, world, pos, context); }
        if (!(entityContext.getEntity() instanceof Player player)) { return this.getShape(state, world, pos, context); }

        if (!player.level().isClientSide()) {
            GameManager gameManager = GameManager.getInstance();
            if (gameManager.getData().getState() == GAME_STATE.PREPARING) {
                PlayerGameData playerGameData = gameManager.getData().getPlayerData(player.getUUID());
                if (playerGameData != null && playerGameData.getRole() == GAME_ROLE.SEEKER) {
                    return this.getShape(state, world, pos, context);
                }
            }
        }

        return Shapes.empty();
    }
}