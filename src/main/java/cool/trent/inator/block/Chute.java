package cool.trent.inator.block;

import cool.trent.inator.block.entity.ChuteEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class Chute extends ConnectingBlock implements BlockEntityProvider  {
    public static final DirectionProperty DIRECTION = DirectionProperty.of("direction", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN);
    public static final BooleanProperty ENABLED = BooleanProperty.of("enabled");

    public Chute() {
        super(0.3125F, FabricBlockSettings
                .of(Material.METAL)
                .strength(1.0f, 15.0f)
                .breakByHand(true)
                .sounds(BlockSoundGroup.LANTERN)
        );
        setDefaultState(getStateManager().getDefaultState()
                .with(NORTH, false)
                .with(EAST , false)
                .with(SOUTH, false)
                .with(WEST , false)
                .with(UP   , true)
                .with(DOWN , true)
                .with(DIRECTION, Direction.NORTH)
                .with(ENABLED, true)
        );
    }


    // Set state when placing
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        return this.withConnectionProperties(ctx.getWorld(), ctx.getBlockPos());
    }
    // Set state when updating
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
        return this.withConnectionProperties(world, pos);
    }

    // Updates side connection states
    // Modified from ChorusPlantBlock
    public BlockState withConnectionProperties(BlockView world, BlockPos pos) {
        Block blockDown = world.getBlockState(pos.down()).getBlock();
        Block blockUp = world.getBlockState(pos.up()).getBlock();
        Block blockNorth = world.getBlockState(pos.north()).getBlock();
        Block blockEast = world.getBlockState(pos.east()).getBlock();
        Block blockSouth = world.getBlockState(pos.south()).getBlock();
        Block blockWest = world.getBlockState(pos.west()).getBlock();

        boolean isChuteDown = blockDown == this;
        boolean isChuteUp = blockUp == this;
        boolean isChuteNorth = blockNorth == this;
        boolean isChuteEast = blockEast == this;
        boolean isChuteSouth = blockSouth == this;
        boolean isChuteWest = blockWest == this;

        boolean yOnly = !(isChuteNorth || isChuteEast || isChuteSouth || isChuteWest);
        boolean xOnly = !yOnly && !(isChuteUp || isChuteNorth || isChuteDown || isChuteSouth);
        boolean zOnly = !yOnly && !xOnly && !(isChuteUp || isChuteEast || isChuteDown || isChuteWest);

        // This returns the conditions
        return this.getDefaultState()
                .with(DOWN, isChuteDown || yOnly)
                .with(UP, isChuteUp || yOnly)
                .with(NORTH, isChuteNorth || zOnly)
                .with(EAST, isChuteEast || xOnly)
                .with(SOUTH, isChuteSouth || zOnly)
                .with(WEST, isChuteWest || xOnly)
                .with(DIRECTION, Direction.NORTH) //TODO: Direction only flows north
                .with(ENABLED, true); // TODO: Add disabling
    }

    // Needed?
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(NORTH);
        stateManager.add(EAST);
        stateManager.add(SOUTH);
        stateManager.add(WEST);
        stateManager.add(UP);
        stateManager.add(DOWN);
        stateManager.add(DIRECTION);
        stateManager.add(ENABLED);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        return new ChuteEntity();
    }


    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;

        Inventory blockEntity = (Inventory) world.getBlockEntity(pos);
        ItemStack playerStackInHand = player.getStackInHand(hand);


        // This could easily be rewritten more concisely and more reusable
        if (!playerStackInHand.isEmpty()) {
            // Fill item from player's hand into first empty slot
            if (blockEntity.getStack(0).isEmpty()) {
                blockEntity.setStack(0, playerStackInHand.copy()); // Note that copy() is used
                playerStackInHand.setCount(0);
            } else if (blockEntity.getStack(1).isEmpty()) {
                blockEntity.setStack(1, playerStackInHand.copy());
                playerStackInHand.setCount(0);
            } else {
                // Inventory is full; print the contents
                System.out.printf("Slot 0: %s\nSlot 1: %s\n", blockEntity.getStack(0).toString(), blockEntity.getStack(1).toString());
            }
        } else {
            // Player is not holding anything; return item
            if (!blockEntity.getStack(1).isEmpty()) {
                player.inventory.offerOrDrop(world, blockEntity.getStack(1));
                blockEntity.removeStack(1);
            } else if (!blockEntity.getStack(0).isEmpty()) {
                player.inventory.offerOrDrop(world, blockEntity.getStack(0));
                blockEntity.removeStack(0);
            }
        }

        return ActionResult.SUCCESS;
    }

}
