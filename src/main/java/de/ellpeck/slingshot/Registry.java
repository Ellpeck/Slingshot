package de.ellpeck.slingshot;

import de.ellpeck.slingshot.entity.GunpowderProjectile;
import de.ellpeck.slingshot.entity.EntityProjectile;
import de.ellpeck.slingshot.entity.PlacingProjectile;
import de.ellpeck.slingshot.entity.ShotgunProjectile;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
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

    public static EntityType<PlacingProjectile> placingProjectile;
    public static EntityType<GunpowderProjectile> gunpowderProjectile;
    public static EntityType<ShotgunProjectile> shotgunProjectile;

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
            GunpowderProjectile projectile = new GunpowderProjectile(gunpowderProjectile, player, player.world, charged);
            projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0, 0.65F, 0);
            world.addEntity(projectile);
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
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(slingshot = new ItemSlingshot());
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().registerAll(
                placingProjectile = buildProjectile("placing", PlacingProjectile::new),
                gunpowderProjectile = buildProjectile("gunpowder", GunpowderProjectile::new),
                shotgunProjectile = buildProjectile("shotgun", ShotgunProjectile::new)
        );
    }

    private static <T extends EntityProjectile> EntityType<T> buildProjectile(String name, EntityType.IFactory<T> factory) {
        return (EntityType<T>) EntityType.Builder
                .create(factory, EntityClassification.MISC)
                .size(0.25F, 0.25F).setShouldReceiveVelocityUpdates(true).setTrackingRange(64)
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

            RenderingRegistry.registerEntityRenderingHandler(PlacingProjectile.class, manager -> new SpriteRenderer<>(manager, renderer, 0.35F));
            RenderingRegistry.registerEntityRenderingHandler(GunpowderProjectile.class, manager -> new SpriteRenderer<>(manager, renderer, 0.35F));
            RenderingRegistry.registerEntityRenderingHandler(ShotgunProjectile.class, manager -> new SpriteRenderer<>(manager, renderer, 0.15F));
        }
    }
}
