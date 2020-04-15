package de.ellpeck.slingshot.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.network.IPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.network.NetworkHooks;

public class SeedProjectile extends EntityProjectile {
    public SeedProjectile(EntityType<? extends ProjectileItemEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public SeedProjectile(EntityType<? extends ProjectileItemEntity> type, LivingEntity entity, World worldIn) {
        super(type, entity, worldIn);
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        plant:
        if (result.getType() == RayTraceResult.Type.BLOCK) {
            BlockRayTraceResult blockResult = (BlockRayTraceResult) result;
            if (blockResult.getFace() != Direction.UP)
                break plant;
            BlockPos pos = blockResult.getPos();
            BlockPos up = pos.up();
            if (!this.world.getBlockState(up).getMaterial().isReplaceable())
                break plant;
            BlockState plant = ((BlockItem) this.getItem().getItem()).getBlock().getDefaultState();
            if (!plant.isValidPosition(this.world, up))
                break plant;
            this.world.setBlockState(up, plant);
            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ITEM_CROP_PLANT, SoundCategory.BLOCKS, 1, 1);
            this.remove();
            return;
        }
        super.onImpact(result);
    }
}
