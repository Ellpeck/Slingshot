package de.ellpeck.slingshot.entity;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.ParticleArgument;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class EffectCloudProjectile extends EntityProjectile {

    public float radius;
    public int duration;
    public IParticleData particleType;
    public EffectInstance[] effects;
    public float damagePerSecond;
    public boolean ignitesEntities;
    public boolean canBeLit;

    public EffectCloudProjectile(EntityType<? extends ProjectileItemEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public EffectCloudProjectile(EntityType<? extends ProjectileItemEntity> type, LivingEntity entity, World worldIn, ItemStack stack) {
        super(type, entity, worldIn, stack);
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (!this.world.isRemote) {
            SpecialEffectCloudEntity effect = new SpecialEffectCloudEntity(this.world, this.posX, this.posY, this.posZ);
            effect.setRadius(this.radius);
            effect.setDuration(this.duration);
            effect.setParticleData(this.particleType);
            effect.damagePerSecond = this.damagePerSecond;
            effect.ignitesEntities = this.ignitesEntities;
            effect.canBeLit = this.canBeLit;
            for (EffectInstance e : this.effects)
                effect.addEffect(new EffectInstance(e));
            this.world.addEntity(effect);
        }
        this.remove();
    }

    @Override
    public void writeAdditional(CompoundNBT nbt) {
        super.writeAdditional(nbt);
        nbt.putFloat("radius", this.radius);
        nbt.putInt("duration", this.duration);
        nbt.putString("particle", this.particleType.getParameters());
        ListNBT list = new ListNBT();
        for (EffectInstance effect : this.effects)
            list.add(effect.write(new CompoundNBT()));
        nbt.put("effects", list);
        nbt.putBoolean("ignites", this.ignitesEntities);
        nbt.putFloat("damage", this.damagePerSecond);
        nbt.putBoolean("can_be_lit", this.canBeLit);
    }

    @Override
    public void readAdditional(CompoundNBT nbt) {
        super.readAdditional(nbt);
        this.radius = nbt.getFloat("radius");
        this.duration = nbt.getInt("duration");
        try {
            this.particleType = ParticleArgument.parseParticle(new StringReader(nbt.getString("particle")));
        } catch (CommandSyntaxException ignored) {
        }
        ListNBT list = nbt.getList("effects", Constants.NBT.TAG_STRING);
        this.effects = new EffectInstance[list.size()];
        for (int i = 0; i < list.size(); i++)
            this.effects[i] = EffectInstance.read(list.getCompound(i));
        this.ignitesEntities = nbt.getBoolean("ignites");
        this.damagePerSecond = nbt.getFloat("damage");
        this.canBeLit = nbt.getBoolean("can_be_lit");
    }
}
