package cool.trent.inator.block.entity;

import cool.trent.inator.block.Chute;
import cool.trent.inator.common.BlockEntities;
import cool.trent.inator.inventory.ChuteInventory;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Much of this is based on the HopperBlockEntity class file
 */
public class ChuteEntity extends BlockEntity implements Tickable, ChuteInventory {
    private int speed = 6;
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(2, ItemStack.EMPTY);


    public ChuteEntity() {
        super(BlockEntities.CHUTE_ENTITY);
    }


    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }


    @Override
    public void tick() {
        if (world != null && !world.isClient) {
            insertAndExtract(() -> extract(this, world, getPos()));
        }
    }

    private boolean insertAndExtract(Supplier<Boolean> extractMethod) {
        if (this.world != null && !this.world.isClient) {
            if (this.getCachedState().get(Chute.ENABLED)) { // Proceed if enabled
                boolean bl = false;

                if (!this.isEmpty()) {
                    bl = this.insert();
                }

                if (!this.isFull()) {
                    bl |= extractMethod.get();
                }

                if (bl) {
                    markDirty();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determine whether this inventory is full
     * @return true if full
     */
    private boolean isFull() {
        Iterator var1 = this.items.iterator();

        ItemStack itemStack;
        do {
            if (!var1.hasNext()) {
                return true;
            }

            itemStack = (ItemStack)var1.next();
        } while(!itemStack.isEmpty() && itemStack.getCount() == itemStack.getMaxCount());

        return false;
    }

    private boolean insert() {
        Inventory inventory = this.getOutputInventory();
        if (inventory == null)
            return false;

        Direction direction = (this.getCachedState().get(Chute.DIRECTION)).getOpposite();
        if (!this.isInventoryFull(inventory, direction)) {
            for (int i = 0; i < this.size(); ++i) {
                if (!this.getStack(i).isEmpty()) {
                    ItemStack itemStack = this.getStack(i).copy();
                    ItemStack itemStack2 = transfer(inventory, this.removeStack(i, 1), direction);
                    if (itemStack2.isEmpty()) {
                        inventory.markDirty();
                        return true;
                    }

                    this.setStack(i, itemStack);
                }
            }
        }

        return false;
    }

    private boolean isInventoryFull(Inventory inv, Direction direction) {
        return getAvailableSlots(inv, direction).allMatch((i) -> {
            ItemStack itemStack = inv.getStack(i);
            return itemStack.getCount() >= itemStack.getMaxCount();
        });
    }

    @Nullable
    private Inventory getOutputInventory() {
        Direction direction = this.getCachedState().get(Chute.DIRECTION);
        return getInventoryAt(this.getWorld(), this.pos.offset(direction));
    }

    public static boolean extract(ChuteInventory chuteInventory, World world, BlockPos pos) {
        Inventory inventory = getInputInventory(world, pos);

        if (inventory != null) { // If the inventory exists
            Direction direction = Direction.DOWN; // We are extracting from the bottom (DOWN side)
            // Returns true if inventory is not empty and any slots match extract result (?)
            return !isInventoryEmpty(inventory, direction) && getAvailableSlots(inventory, direction).anyMatch((i) -> extract(chuteInventory, inventory, i, direction));
        } else {
            // World pickup could go here?
            return true;
        }
    }
    private static boolean extract(ChuteInventory chuteInventory, Inventory inventory, int slot, Direction side) {
        ItemStack itemStack = inventory.getStack(slot);
        if (!itemStack.isEmpty() && canExtract(inventory, itemStack, slot, side)) {
            ItemStack itemStack2 = itemStack.copy();
            ItemStack itemStack3 = transfer(chuteInventory, inventory.removeStack(slot, 1), null);
            if (itemStack3.isEmpty()) {
                inventory.markDirty();
                return true;
            }

            inventory.setStack(slot, itemStack2);
        }

        return false;
    }

    /**
     * Check if target inventory is empty
     * @param inv    Inventory to check
     * @param facing Facing direction
     * @return true if all slots are empty
     */
    private static boolean isInventoryEmpty(Inventory inv, Direction facing) {
        return getAvailableSlots(inv, facing).allMatch((i) -> {
            return inv.getStack(i).isEmpty();
        });
    }

    /**
     * Get the available slots from a SidedInventory
     * @param inventory Inventory to check
     * @param side      Side to check
     * @return IntStream of available slots
     */
    private static IntStream getAvailableSlots(Inventory inventory, Direction side) {
        return inventory instanceof SidedInventory
                ? IntStream.of(((SidedInventory)inventory).getAvailableSlots(side))
                : IntStream.range(0, inventory.size());
    }

    /**
     * Transfer stack from one inventory to another
     * @param to
     * @param stack
     * @param side
     * @return
     */
    public static ItemStack transfer(Inventory to, ItemStack stack, @Nullable Direction side) {
        if (to instanceof SidedInventory && side != null) {
            SidedInventory sidedInventory = (SidedInventory)to;
            int[] is = sidedInventory.getAvailableSlots(side);

            for(int i = 0; i < is.length && !stack.isEmpty(); ++i) {
                stack = transfer(to, stack, is[i], side);
            }
        } else {
            int j = to.size();

            for(int k = 0; k < j && !stack.isEmpty(); ++k) {
                stack = transfer(to, stack, k, side);
            }
        }

        return stack;
    }

    /**
     * Transfer stack from one inventory to another
     * @param to        Inventory to transfer to
     * @param stack     ItemStack
     * @param slot      Slot
     * @return
     */
    private static ItemStack transfer(Inventory to, ItemStack stack, int slot, Direction direction) {
        ItemStack itemStack = to.getStack(slot);
        if (canInsert(to, stack, slot, direction)) {
            boolean didTransfer = false;
            if (itemStack.isEmpty()) { // Target slot is empty; can transfer
                to.setStack(slot, stack);
                stack = ItemStack.EMPTY;
                didTransfer = true;
            } else if (canMergeItems(itemStack, stack)) { // Target slot can be merged; can transfer
                int i = stack.getMaxCount() - itemStack.getCount();
                int j = Math.min(stack.getCount(), i);
                stack.decrement(j);
                itemStack.increment(j);
                didTransfer = j > 0;
            }

            if (didTransfer) {
                to.markDirty();
            }
        }

        return stack;
    }

    /**
     * Determine whether an ItemStack can be insterted into an inventory
     * @param inventory Inventory to check
     * @param stack     Itemstack to check
     * @param slot      Slot
     * @param side      Side
     * @return true if can
     */
    private static boolean canInsert(Inventory inventory, ItemStack stack, int slot, @Nullable Direction side) {
        if (!inventory.isValid(slot, stack)) {
            return false;
        } else {
            return !(inventory instanceof SidedInventory) || ((SidedInventory) inventory).canInsert(slot, stack, side);
        }
    }

    /**
     * Determine if two stacks can be merged
     * @param first  ItemStack 1
     * @param second ItemStack 2
     * @return true if can
     */
    private static boolean canMergeItems(ItemStack first, ItemStack second) {
        if (first.getItem() != second.getItem()) {
            return false;
        } else if (first.getDamage() != second.getDamage()) {
            return false;
        } else if (first.getCount() > first.getMaxCount()) {
            return false;
        } else {
            return ItemStack.areTagsEqual(first, second);
        }
    }

    /**
     * Determine whether inventory is extractable
     * @param inv    Inventory to check
     * @param stack
     * @param slot
     * @param facing
     * @return true if can extract
     */
    private static boolean canExtract(Inventory inv, ItemStack stack, int slot, Direction facing) {
        return !(inv instanceof SidedInventory) || ((SidedInventory)inv).canExtract(slot, stack, facing);
    }

    /**
     * Returns the inventory of the above block, blockEntity, or entity with valid inventory
     * @param world World to check
     * @param pos Position of block
     * @return Inventory of valid input
     */
    @Nullable
    public static Inventory getInputInventory(World world, BlockPos pos) {
        return getInventoryAt(world, pos.getX(), pos.getY() + 1.0D, pos.getZ());
    }

    @Nullable
    public static Inventory getInventoryAt(World world, BlockPos blockPos) {
        return getInventoryAt(world, (double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D);
    }
    @Nullable
    public static Inventory getInventoryAt(World world, double x, double y, double z) {
        Inventory inventory = null;
        BlockPos blockPos = new BlockPos(x, y, z);
        BlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();

        // To get the inventory from the world, we first try...
        // ...InventoryProvider blocks...
        if (block instanceof InventoryProvider) {
            inventory = ((InventoryProvider) block).getInventory(blockState, world, blockPos);
        } else if (block.hasBlockEntity()) { // ... then BlockEntities with inventories
            BlockEntity blockEntity = world.getBlockEntity(blockPos);
            if (blockEntity instanceof Inventory) {
                inventory = (Inventory) blockEntity;
                if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock) {
                    inventory = ChestBlock.getInventory((ChestBlock) block, blockState, world, blockPos, true);
                }
            }
        }

        // If both above cases fail, we finally try for VALID_INVENTORY
        if (inventory == null) {
            List<Entity> list = world.getOtherEntities(null, new Box(x - 0.5D, y - 0.5D, z - 0.5D, x + 0.5D, y + 0.5D, z + 0.5D), EntityPredicates.VALID_INVENTORIES);
            if (!list.isEmpty()) {
                inventory = (Inventory) list.get(world.random.nextInt(list.size()));
            }
        }

        return inventory;
    }

    // toTag method saves data so its persistent when unloading
    // it is called whenever a chunk is marked as dirty
    // to mark a chunk dirty, call markDirty() whenever a change is made to a custom variable
    @Override
    public CompoundTag toTag(CompoundTag tag) {
        // Save current value of number to tag
        tag.putInt("speed", speed);

        // Save inventory
        Inventories.toTag(tag, items);

        return super.toTag(tag); // super.toTag(tag) ends up returning tag I think
    }

    // fromTag loads data saved via toTag when loading
    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        Inventories.fromTag(tag, items);
        // Load saved value of number from tag
        speed = tag.getInt("speed");
    }
}
