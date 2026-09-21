package eu.ha3.matmos;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;
import eu.ha3.mc.haddon.forge.ForgeBase;

@Mod(modid = ForgeMatmos.MODID, name = ForgeMatmos.NAME, version = ForgeMatmos.VERSION,
        acceptedMinecraftVersions = "[1.7.10]", dependencies = "required-after:unimixins",
        guiFactory = "eu.ha3.matmos.gui.MAtmosGuiFactory",
        customProperties = {
                @Mod.CustomProperty(k = "license", v = "WTFPL-2.0"),
                @Mod.CustomProperty(k = "issueTrackerUrl", v = "https://github.com/JackOfNoneTrades/MAtmos/issues"),
                @Mod.CustomProperty(k = "iconFile", v = "assets/matmos/icon.png"),
                @Mod.CustomProperty(k = "backgroundFile", v = "assets/matmos/background.png") })
public class ForgeMatmos extends ForgeBase {
    public static final Matmos MATMOS = new Matmos();
    public static final String MODID = "matmos";
    public static final String NAME = Matmos.NAME;
    public static final String VERSION = Matmos.VERSION;

    public ForgeMatmos() {
        super(MATMOS);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        super.onClientTick(event);
    }

    @SubscribeEvent
    public void onRenderTick(RenderTickEvent event) {
        super.onRenderTick(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        super.init(event, MODID, NAME, VERSION);
    }

}
