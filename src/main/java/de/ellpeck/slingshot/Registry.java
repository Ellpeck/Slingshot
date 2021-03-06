package de.ellpeck.slingshot;

import de.ellpeck.slingshot.entity.*;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.AreaEffectCloudRenderer;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Registry {

    private static final List<SlingshotBehavior> BEHAVIORS = new ArrayList<>();
    public static Item slingshot;

    public static EntityType<EntityProjectile> projectileEntity;
    public static EntityType<PlacingProjectile> placingProjectile;
    public static EntityType<GunpowderProjectile> gunpowderProjectile;
    public static EntityType<ShotgunProjectile> shotgunProjectile;
    public static EntityType<EffectCloudProjectile> effectCloudProjectile;
    public static EntityType<SpecialEffectCloudEntity> effectCloudEntity;

    public static Enchantment capacityEnchantment;
    public static Enchantment reloadEnchantment;
    public static Enchantment ignitionEnchantment;

    public static SlingshotBehavior tntBehavior;

    public static void setup(FMLCommonSetupEvent event) {
        addPlaceBehavior("carrot", new ItemStack(Items.CARROT), 40, 3, 0.85F, null);
        addPlaceBehavior("potato", new ItemStack(Items.POTATO), 40, 3, 0.8F, null);
        addPlaceBehavior("wheat_seeds", new ItemStack(Items.WHEAT_SEEDS), 20, 0.75F, 0.45F, null);
        addPlaceBehavior("beetroot_seeds", new ItemStack(Items.BEETROOT_SEEDS), 20, 0.75F, 0.45F, null);
        addPlaceBehavior("melon_seeds", new ItemStack(Items.MELON_SEEDS), 20, 0.75F, 0.45F, null);
        addPlaceBehavior("pumpkin_seeds", new ItemStack(Items.PUMPKIN_SEEDS), 20, 0.75F, 0.45F, null);
        addBehavior(new SlingshotBehavior("ender_pearl", new ItemStack(Items.ENDER_PEARL), 30, (world, player, stack, charged, item) -> {
            EnderPearlEntity pearl = new EnderPearlEntity(world, player);
            pearl.setItem(charged.copy());
            pearl.shoot(player, player.rotationPitch, player.rotationYaw, 0, 2.5F, 1);
            world.addEntity(pearl);
        }));
        addPlaceBehavior("torch", new ItemStack(Blocks.TORCH), 50, 6, 2, p -> p.fireChance = 0.25F);
        addPlaceBehavior("redstone_torch", new ItemStack(Blocks.REDSTONE_TORCH), 50, 6, 2, null);
        addBehavior(new SlingshotBehavior("gunpowder", new ItemStack(Items.GUNPOWDER), 40, (world, player, stack, charged, item) -> {
            if (EnchantmentHelper.getEnchantmentLevel(ignitionEnchantment, stack) > 0) {
                world.createExplosion(null, player.posX, player.posY - 0.5F, player.posZ, 0.5F, Explosion.Mode.NONE);
            } else {
                GunpowderProjectile projectile = new GunpowderProjectile(gunpowderProjectile, player, player.world, charged);
                projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0, 0.65F, 0);
                world.addEntity(projectile);
            }
        }));
        addPlaceBehavior("oak_sapling", new ItemStack(Blocks.OAK_SAPLING), 40, 5, 0.8F, null);
        addPlaceBehavior("dark_oak_sapling", new ItemStack(Blocks.DARK_OAK_SAPLING), 40, 5, 0.8F, null);
        addPlaceBehavior("acacia_sapling", new ItemStack(Blocks.ACACIA_SAPLING), 40, 5, 0.8F, null);
        addPlaceBehavior("spruce_sapling", new ItemStack(Blocks.SPRUCE_SAPLING), 40, 5, 0.8F, null);
        addPlaceBehavior("jungle_sapling", new ItemStack(Blocks.JUNGLE_SAPLING), 40, 5, 0.8F, null);
        addPlaceBehavior("birch_sapling", new ItemStack(Blocks.BIRCH_SAPLING), 40, 5, 0.8F, null);
        addBehavior(new SlingshotBehavior("gravel", new ItemStack(Blocks.GRAVEL), 70, (world, player, stack, charged, item) -> {
            for (int i = 0; i < 10; i++) {
                ShotgunProjectile projectile = new ShotgunProjectile(shotgunProjectile, player, player.world, charged);
                projectile.setDamage(4);
                projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0, 0.8F, 20);
                world.addEntity(projectile);
            }
        }));
        addCloudBehavior("sand", new ItemStack(Blocks.SAND), 40, 0.45F, 2.5F, 60, false, 1, false, new BlockParticleData(ParticleTypes.FALLING_DUST, Blocks.SAND.getDefaultState()), new EffectInstance(Effects.BLINDNESS, 60));
        addCloudBehavior("red_sand", new ItemStack(Blocks.RED_SAND), 40, 0.45F, 2.5F, 60, false, 1, false, new BlockParticleData(ParticleTypes.FALLING_DUST, Blocks.RED_SAND.getDefaultState()), new EffectInstance(Effects.BLINDNESS, 60));
        addCloudBehavior("soul_sand", new ItemStack(Blocks.SOUL_SAND), 60, 0.45F, 2.5F, 60, false, 2, false, new BlockParticleData(ParticleTypes.FALLING_DUST, Blocks.SOUL_SAND.getDefaultState()), new EffectInstance(Effects.WITHER, 60));
        addCloudBehavior("redstone", new ItemStack(Items.REDSTONE), 30, 0.45F, 2.5F, 60, false, 0, false, new BlockParticleData(ParticleTypes.FALLING_DUST, Blocks.REDSTONE_BLOCK.getDefaultState()), new EffectInstance(Effects.SLOWNESS, 30, 255));
        addCloudBehavior("glowstone", new ItemStack(Items.GLOWSTONE_DUST), 30, 0.45F, 2.5F, 60, false, 0, false, new BlockParticleData(ParticleTypes.FALLING_DUST, Blocks.GLOWSTONE.getDefaultState()), new EffectInstance(Effects.GLOWING, 200));
        addCloudBehavior("blaze_powder", new ItemStack(Items.BLAZE_POWDER), 30, 0.45F, 2.5F, 20, true, 4, false, ParticleTypes.FLAME);
        addCloudBehavior("wheat", new ItemStack(Items.WHEAT), 40, 0.45F, 2.5F, 100, false, 0, true, new BlockParticleData(ParticleTypes.FALLING_DUST, Blocks.SAND.getDefaultState()), new EffectInstance(Effects.BLINDNESS, 60));
        addBehavior(tntBehavior = new SlingshotBehavior("tnt", new ItemStack(Blocks.TNT), 20 * 5, (world, player, stack, charged, item) -> {
            TNTEntity tnt = new TNTEntity(world, player.posX, player.posY, player.posZ, player);
            int litTime = (int) (world.getGameTime() - ItemSlingshot.getLightTime(stack));
            tnt.setFuse(20 * 4 - litTime);
            tnt.world.addEntity(tnt);

            // ThrowableEntity#shoot copypasta
            float f0 = -MathHelper.sin(player.rotationYaw * ((float) Math.PI / 180F)) * MathHelper.cos(player.rotationPitch * ((float) Math.PI / 180F));
            float f1 = -MathHelper.sin((player.rotationPitch) * ((float) Math.PI / 180F));
            float f2 = MathHelper.cos(player.rotationYaw * ((float) Math.PI / 180F)) * MathHelper.cos(player.rotationPitch * ((float) Math.PI / 180F));
            Vec3d vec3d = (new Vec3d(f0, f1, f2)).normalize().scale(0.85F);
            tnt.setMotion(vec3d);
            float f = MathHelper.sqrt(Entity.horizontalMag(vec3d));
            tnt.rotationYaw = (float) (MathHelper.atan2(vec3d.x, vec3d.z) * (double) (180F / (float) Math.PI));
            tnt.rotationPitch = (float) (MathHelper.atan2(vec3d.y, f) * (double) (180F / (float) Math.PI));
            tnt.prevRotationYaw = tnt.rotationYaw;
            tnt.prevRotationPitch = tnt.rotationPitch;
            Vec3d vec3d2 = player.getMotion();
            tnt.setMotion(tnt.getMotion().add(vec3d2.x, player.onGround ? 0.0D : vec3d2.y, vec3d2.z));
        }));
        addBehavior(new SlingshotBehavior("golden_carrot", new ItemStack(Items.GOLDEN_CARROT), 50, (world, player, stack, charged, item) -> {
            EntityProjectile projectile = new EntityProjectile(projectileEntity, player, world, charged);
            projectile.setDamage(8);
            projectile.dropItem = true;
            projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0, 0.9F, 0);
            world.addEntity(projectile);
        }));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(slingshot = new ItemSlingshot());
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().registerAll(
                projectileEntity = buildEntity("projectile", EntityProjectile::new, 0.25F, 0.25F),
                placingProjectile = buildEntity("placing", PlacingProjectile::new, 0.25F, 0.25F),
                gunpowderProjectile = buildEntity("gunpowder", GunpowderProjectile::new, 0.25F, 0.25F),
                shotgunProjectile = buildEntity("shotgun", ShotgunProjectile::new, 0.25F, 0.25F),
                effectCloudProjectile = buildEntity("effect_cloud", EffectCloudProjectile::new, 0.25F, 0.25F),
                effectCloudEntity = buildEntity("special_effect_cloud", SpecialEffectCloudEntity::new, 6, 0.5F)
        );
    }

    @SubscribeEvent
    public static void registerEnchants(RegistryEvent.Register<Enchantment> event) {
        event.getRegistry().registerAll(
                capacityEnchantment = new SlingshotEnchantment(Enchantment.Rarity.UNCOMMON, 5, 1, 12, 20).setRegistryName("capacity"),
                reloadEnchantment = new SlingshotEnchantment(Enchantment.Rarity.UNCOMMON, 6, 1, 12, 20).setRegistryName("reload"),
                ignitionEnchantment = new SlingshotEnchantment(Enchantment.Rarity.UNCOMMON, 1, 20, 0, 50).setRegistryName("ignition")
        );
    }

    private static <T extends Entity> EntityType<T> buildEntity(String name, EntityType.IFactory<T> factory, float width, float height) {
        return (EntityType<T>) EntityType.Builder
                .create(factory, EntityClassification.MISC)
                .size(width, height).setShouldReceiveVelocityUpdates(true).setTrackingRange(64)
                .setUpdateInterval(3).build(Slingshot.ID + ":" + name).setRegistryName(name);
    }

    public static void addBehavior(SlingshotBehavior behavior) {
        BEHAVIORS.add(behavior);
        slingshot.addPropertyOverride(new ResourceLocation(Slingshot.ID, behavior.name), (stack, world, entity) -> entity != null && ItemSlingshot.getChargedItem(stack).isItemEqual(behavior.item) ? 1 : 0);
    }

    private static void addPlaceBehavior(String name, ItemStack stack, int chargeTime, float damage, float velocity, Consumer<PlacingProjectile> projectileModifier) {
        addBehavior(new SlingshotBehavior(name, stack, chargeTime, (world, player, stacc, charged, item) -> {
            PlacingProjectile projectile = new PlacingProjectile(placingProjectile, player, world, charged);
            projectile.setDamage(damage);
            projectile.dropItem = true;
            projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0, velocity, 0);
            if (projectileModifier != null)
                projectileModifier.accept(projectile);
            world.addEntity(projectile);
        }));
    }

    private static void addCloudBehavior(String name, ItemStack stack, int chargeTime, float velocity, float radius, int duration, boolean ignites, float damage, boolean canBeLit, IParticleData particleType, EffectInstance... effects) {
        addBehavior(new SlingshotBehavior(name, stack, chargeTime, (world, player, stacc, charged, item) -> {
            EffectCloudProjectile projectile = new EffectCloudProjectile(effectCloudProjectile, player, world, charged);
            projectile.radius = radius;
            projectile.duration = duration;
            projectile.particleType = particleType;
            projectile.effects = effects;
            projectile.ignitesEntities = ignites;
            projectile.damagePerSecond = damage;
            projectile.canBeLit = canBeLit;
            if (canBeLit && EnchantmentHelper.getEnchantmentLevel(ignitionEnchantment, stacc) > 0) {
                projectile.ignitesEntities = true;
                projectile.particleType = ParticleTypes.FLAME;
            }
            projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0, velocity, 0);
            world.addEntity(projectile);
        }));
    }

    public static SlingshotBehavior getBehavior(ItemStack stack) {
        if (stack.isEmpty())
            return null;
        for (SlingshotBehavior behavior : BEHAVIORS) {
            if (behavior.item.isItemEqual(stack))
                return behavior;
        }
        return null;
    }

    public static final class Client {
        public static void setup(FMLClientSetupEvent event) {
            Minecraft mc = event.getMinecraftSupplier().get();
            ItemRenderer renderer = mc.getItemRenderer();

            RenderingRegistry.registerEntityRenderingHandler(EntityProjectile.class, manager -> new SpriteRenderer<>(manager, renderer, 0.35F));
            RenderingRegistry.registerEntityRenderingHandler(PlacingProjectile.class, manager -> new SpriteRenderer<>(manager, renderer, 0.35F));
            RenderingRegistry.registerEntityRenderingHandler(GunpowderProjectile.class, manager -> new SpriteRenderer<>(manager, renderer, 0.35F));
            RenderingRegistry.registerEntityRenderingHandler(ShotgunProjectile.class, manager -> new SpriteRenderer<>(manager, renderer, 0.15F));
            RenderingRegistry.registerEntityRenderingHandler(EffectCloudProjectile.class, manager -> new SpriteRenderer<>(manager, renderer, 0.35F));
            RenderingRegistry.registerEntityRenderingHandler(SpecialEffectCloudEntity.class, AreaEffectCloudRenderer::new);
        }
    }
}
