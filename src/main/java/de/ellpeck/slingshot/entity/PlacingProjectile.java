package de.ellpeck.slingshot.entity;

import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.*;
import net.minecraft.network.IPacket;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.network.NetworkHooks;

public class PlacingProjectile extends EntityProjectile {
    public PlacingProjectile(EntityType<? extends ProjectileItemEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public PlacingProjectile(EntityType<? extends ProjectileItemEntity> type, LivingEntity entity, World worldIn, ItemStack stack) {
        super(type, entity, worldIn, stack);
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        place:
        if (!this.world.isRemote && result.getType() == RayTraceResult.Type.BLOCK) {
            BlockRayTraceResult blockResult = (BlockRayTraceResult) result;
            BlockPos pos = blockResult.getPos();
            BlockPos off = pos.offset(blockResult.getFace());
            if (!this.world.getBlockState(off).getMaterial().isReplaceable())
                break place;
            // how can it be this hard to place a torch and make it actually attach to a wall...
            PlayerEntity player = (PlayerEntity) this.getThrower();
            BlockItem item = (BlockItem) this.getItem().getItem();
            BlockRayTraceResult placeResult = new BlockRayTraceResult(blockResult.getHitVec(), blockResult.getFace(), off, false);
            ItemStack holding = player.getHeldItemMainhand().copy();
            ActionResultType action = item.tryPlace(new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, placeResult)));
            // we have to put the item back because the only method that allows us to place
            // something properly also takes away the item from the player's hand...
            player.setHeldItem(Hand.MAIN_HAND, holding);
            if (action != ActionResultType.SUCCESS)
                break place;
            this.remove();
            return;
        }
        super.onImpact(result);
    }
}
