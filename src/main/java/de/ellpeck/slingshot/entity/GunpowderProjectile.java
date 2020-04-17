package de.ellpeck.slingshot.entity;

import de.ellpeck.slingshot.Slingshot;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class GunpowderProjectile extends EntityProjectile {
    public GunpowderProjectile(EntityType<? extends ProjectileItemEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public GunpowderProjectile(EntityType<? extends ProjectileItemEntity> type, LivingEntity entity, World worldIn, ItemStack stack) {
        super(type, entity, worldIn, stack);
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (!this.world.isRemote && result.getType() == RayTraceResult.Type.ENTITY) {
            EntityRayTraceResult res = (EntityRayTraceResult) result;
            res.getEntity().getPersistentData().putBoolean(Slingshot.ID + ":gunpowder", true);
        }
        super.onImpact(result);
    }

    @SubscribeEvent
    public static void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity.getEntityWorld().isRemote)
            return;
        CompoundNBT nbt = entity.getPersistentData();
        if (!nbt.getBoolean(Slingshot.ID + ":gunpowder"))
            return;
        if (!entity.isBurning())
            return;
        nbt.putBoolean(Slingshot.ID + ":gunpowder", false);
        Explosion.Mode mode = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(entity.world, entity) ? Explosion.Mode.DESTROY : Explosion.Mode.NONE;
        entity.world.createExplosion(entity, entity.posX, entity.posY, entity.posZ, 2.5F, mode);
        entity.setHealth(0);
    }
}
