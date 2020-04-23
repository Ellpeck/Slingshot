package de.ellpeck.slingshot.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class SpecialEffectCloudEntity extends AreaEffectCloudEntity {

    public float damagePerSecond;
    public boolean ignitesEntities;
    public boolean canBeLit;

    public SpecialEffectCloudEntity(EntityType<? extends AreaEffectCloudEntity> type, World world) {
        super(type, world);
    }

    public SpecialEffectCloudEntity(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.world.isRemote)
            return;
        if (this.ticksExisted % 5 == 0) {
            float radius = this.getRadius();
            List<Entity> list = this.world.getEntitiesWithinAABB(Entity.class, this.getBoundingBox());
            for (Entity entity : list) {
                double d0 = entity.posX - this.posX;
                double d1 = entity.posZ - this.posZ;
                double d2 = d0 * d0 + d1 * d1;
                if (d2 > radius * radius)
                    continue;
                if (entity instanceof LivingEntity) {
                    if (this.ignitesEntities)
                        entity.setFire(40);
                    if (this.ticksExisted % 20 == 0 && this.damagePerSecond > 0)
                        entity.attackEntityFrom(DamageSource.MAGIC, this.damagePerSecond);
                }
                if (this.canBeLit && !this.ignitesEntities && entity.isBurning()) {
                    this.ignitesEntities = true;
                    this.setParticleData(ParticleTypes.FLAME);
                }
            }

            if (this.canBeLit && !this.ignitesEntities) {
                for (float x = -radius; x <= radius; x++) {
                    for (float z = -radius; z <= radius; z++) {
                        BlockPos pos = this.getPosition().add(x, 0, z);
                        BlockState state = this.world.getBlockState(pos);
                        if (state.isBurning(this.world, pos)) {
                            this.ignitesEntities = true;
                            this.setParticleData(ParticleTypes.FLAME);
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void writeAdditional(CompoundNBT nbt) {
        super.writeAdditional(nbt);
        nbt.putBoolean("ignites", this.ignitesEntities);
        nbt.putFloat("damage", this.damagePerSecond);
        nbt.putBoolean("can_be_lit", this.canBeLit);
    }

    @Override
    protected void readAdditional(CompoundNBT nbt) {
        super.readAdditional(nbt);
        this.ignitesEntities = nbt.getBoolean("ignites");
        this.damagePerSecond = nbt.getFloat("damage");
        this.canBeLit = nbt.getBoolean("can_be_lit");
    }
}
