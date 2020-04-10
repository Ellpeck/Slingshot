package de.ellpeck.slingshot;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Slingshot.ID)
public class Slingshot {

    public static final String ID = "slingshot";
    public static final Logger LOGGER = LogManager.getLogger("Slingshot");

    public Slingshot() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(FMLCommonSetupEvent event) {
        LOGGER.info("Slingshot loading...");
    }
}
