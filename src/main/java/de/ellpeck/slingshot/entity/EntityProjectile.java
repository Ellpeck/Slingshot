package de.ellpeck.slingshot.entity;

import de.ellpeck.slingshot.Registry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityProjectile extends ProjectileItemEntity {

    private static final DataParameter<Float> DAMAGE = EntityDataManager.createKey(EntityProjectile.class, DataSerializers.FLOAT);
    public boolean dropItem;
    public float fireChance;

    public EntityProjectile(EntityType<? extends ProjectileItemEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public EntityProjectile(EntityType<? extends ProjectileItemEntity> type, LivingEntity entity, World worldIn) {
        super(type, entity, worldIn);
    }

    @Override
    protected Item getDefaultItem() {
        // this only seems to be used when we don't set an item manually
        return null;
    }

    public void setDamage(float damage) {
        this.dataManager.set(DAMAGE, damage);
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(DAMAGE, 0F);
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (result.getType() == RayTraceResult.Type.ENTITY) {
            float damage = this.dataManager.get(DAMAGE);
            Entity entity = ((EntityRayTraceResult) result).getEntity();
            entity.attackEntityFrom(DamageSource.causeIndirectDamage(this, this.getThrower()), damage);
            if (this.world.rand.nextFloat() < this.fireChance)
                entity.setFire(60);
        }
        if (this.dropItem)
            this.world.addEntity(new ItemEntity(this.world, this.posX, this.posY, this.posZ, this.getItem()));
        this.remove();
    }

    @Override
    public void writeAdditional(CompoundNBT nbt) {
        super.writeAdditional(nbt);
        nbt.putFloat("damage", this.dataManager.get(DAMAGE));
    }

    @Override
    public void readAdditional(CompoundNBT nbt) {
        super.readAdditional(nbt);
        this.setDamage(nbt.getFloat("damage"));
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
