package com.skirlez.fabricatedexchange.block;

import org.jetbrains.annotations.Nullable;

import com.skirlez.fabricatedexchange.screen.AlchemicalChestScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ChestLidAnimator;
import net.minecraft.block.entity.ViewerCountManager;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BaseChestBlockEntity extends ChestBlockEntity {
    private int viewers = 0;
    private float angle, last;
    private final ViewerCountManager stateManager;
    private final ChestLidAnimator lidAnimator;

    public BaseChestBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.ALCHEMICAL_CHEST, pos, state);
    }

    public BaseChestBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
        this.setInvStackList(DefaultedList.ofSize(size(), ItemStack.EMPTY));
        this.stateManager = new ViewerCountManager() {
            protected void onContainerOpen(World world, BlockPos pos, BlockState state) {
                BaseChestBlockEntity.playSound(world, pos, state, SoundEvents.BLOCK_CHEST_OPEN);
            }

            protected void onContainerClose(World world, BlockPos pos, BlockState state) {
                BaseChestBlockEntity.playSound(world, pos, state, SoundEvents.BLOCK_CHEST_CLOSE);
            }

            protected void onViewerCountUpdate(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
                BaseChestBlockEntity.this.onViewerCountUpdate(world, pos, state, oldViewerCount, newViewerCount);
                viewers = newViewerCount;
                lidAnimator.setOpen(newViewerCount > 0);
            }

            protected boolean isPlayerViewing(PlayerEntity player) {
                if (!(player.currentScreenHandler instanceof AlchemicalChestScreenHandler)) {
                    return false;
                } else {
                    Inventory inventory = ((AlchemicalChestScreenHandler)player.currentScreenHandler).getInventory();
                    return inventory == BaseChestBlockEntity.this;
                }
            }
        };
        this.lidAnimator = new ChestLidAnimator();
    }

    public boolean onSyncedBlockEvent(int type, int data) {
        if (type == 1) {
            this.lidAnimator.setOpen(data > 0);
            return true;
        } else {
            return super.onSyncedBlockEvent(type, data);
        }
    }

    @Override
    public Text getContainerName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    public void onOpen(PlayerEntity player) {
        if (!this.removed && !player.isSpectator()) {
            this.stateManager.openContainer(player, this.getWorld(), this.getPos(), this.getCachedState());
            markDirty();
        }
    }

    public void onClose(PlayerEntity player) {
        if (!this.removed && !player.isSpectator()) {
            this.stateManager.closeContainer(player, this.getWorld(), this.getPos(), this.getCachedState());
            markDirty();
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }


    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        this.viewers = tag.getInt("viewers");
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putInt("viewers", viewers);
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (!this.getWorld().isClient() && this.getWorld() != null && this.getWorld() instanceof ServerWorld world) 
            world.getChunkManager().markForUpdate(getPos());
    }

    @Environment(EnvType.CLIENT)
    public int countViewers() {
        return viewers;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public float getAnimationProgress(float f) {
        return MathHelper.lerp(f, last, angle);
    }

    @Environment(EnvType.CLIENT)
    public void clientTick() {
        last = angle;

        int viewers = countViewers();
        if (viewers == 0 && angle > 0.0F || viewers > 0 && angle < 0.89F) {
            if (viewers > 0) 
                angle += 0.1F;
            else angle -= 0.1F;
                angle = MathHelper.clamp(angle, 0, 1);
        }
    }


    private static void playSound(World world, BlockPos pos, BlockState state, SoundEvent soundEvent) {
        ChestType chestType = state.get(ChestBlock.CHEST_TYPE);
        if (chestType == ChestType.LEFT) {
            return;
        }
        double d = (double)pos.getX() + 0.5;
        double e = (double)pos.getY() + 0.5;
        double f = (double)pos.getZ() + 0.5;
        if (chestType == ChestType.RIGHT) {
            Direction direction = ChestBlock.getFacing(state);
            d += (double)direction.getOffsetX() * 0.5;
            f += (double)direction.getOffsetZ() * 0.5;
        }
        world.playSound(null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5f, world.random.nextFloat() * 0.1f + 0.9f);
    }
}