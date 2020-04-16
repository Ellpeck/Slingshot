package de.ellpeck.slingshot;

import de.ellpeck.slingshot.entity.PlacingProjectile;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.SpriteRenderer;
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

    public static EntityType<PlacingProjectile> seedProjectile;

    public static void setup(FMLCommonSetupEvent event) {
        addPlaceBehavior("carrot", new ItemStack(Items.CARROT), 40, 3, 0.75F, null);
        addPlaceBehavior("potato", new ItemStack(Items.POTATO), 40, 3, 0.75F, null);
        addPlaceBehavior("wheat_seeds", new ItemStack(Items.WHEAT_SEEDS), 20, 0.75F, 0.45F, null);
        addPlaceBehavior("beetroot_seeds", new ItemStack(Items.BEETROOT_SEEDS), 20, 0.75F, 0.45F, null);
        addPlaceBehavior("melon_seeds", new ItemStack(Items.MELON_SEEDS), 20, 0.75F, 0.45F, null);
        addPlaceBehavior("pumpkin_seeds", new ItemStack(Items.PUMPKIN_SEEDS), 20, 0.75F, 0.45F, null);
        addBehavior(new SlingshotBehavior("ender_pearl", new ItemStack(Items.ENDER_PEARL), 30, (world, player, stack, charged, item) -> {
            EnderPearlEntity pearl = new EnderPearlEntity(world, player);
            pearl.setItem(charged);
            pearl.shoot(player, player.rotationPitch, player.rotationYaw, 0, 2.5F, 1);
            return pearl;
        }));
        addPlaceBehavior("torch", new ItemStack(Blocks.TORCH), 50, 6, 2, p -> p.fireChance = 0.25F);
        addPlaceBehavior("redstone_torch", new ItemStack(Blocks.REDSTONE_TORCH), 50, 6, 2, null);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(slingshot = new ItemSlingshot());
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().registerAll(
                seedProjectile = (EntityType<PlacingProjectile>) EntityType.Builder
                        .<PlacingProjectile>create(PlacingProjectile::new, EntityClassification.MISC)
                        .size(0.25F, 0.25F).setShouldReceiveVelocityUpdates(true).setTrackingRange(64)
                        .setUpdateInterval(3).build(Slingshot.ID + ":seed").setRegistryName("seed")
        );
    }

    public static void addBehavior(SlingshotBehavior behavior) {
        BEHAVIORS.add(behavior);
        slingshot.addPropertyOverride(new ResourceLocation(Slingshot.ID, behavior.name), (stack, world, entity) -> entity != null && ItemSlingshot.getChargedItem(stack).isItemEqual(behavior.item) ? 1 : 0);
    }

    private static void addPlaceBehavior(String name, ItemStack stack, int chargeTime, float damage, float velocity, Consumer<PlacingProjectile> projectileModifier) {
        addBehavior(new SlingshotBehavior(name, stack, chargeTime, (world, player, stacc, charged, item) -> {
            PlacingProjectile projectile = new PlacingProjectile(seedProjectile, player, world);
            projectile.setDamage(damage);
            projectile.setItem(charged.copy());
            projectile.dropItem = true;
            projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0, velocity, 0);
            if (projectileModifier != null)
                projectileModifier.accept(projectile);
            return projectile;
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
        }
    }
}
