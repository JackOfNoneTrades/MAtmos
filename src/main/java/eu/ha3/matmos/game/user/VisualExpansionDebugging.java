package eu.ha3.matmos.game.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.lwjgl.opengl.GL11;

import eu.ha3.matmos.Matmos;
import eu.ha3.matmos.core.Provider;
import eu.ha3.matmos.core.ProviderCollection;
import eu.ha3.matmos.core.logic.Visualized;
import eu.ha3.matmos.util.IDontKnowHowToCode;
import eu.ha3.mc.haddon.supporting.SupportsFrameEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class VisualExpansionDebugging implements SupportsFrameEvents {
    private final Matmos mod;
    private final String ex;

    private int GAP = 10;

    public VisualExpansionDebugging(Matmos mod, String ex) {
        this.mod = mod;
        this.ex = ex;
    }

    @Override
    public void onFrame(float semi) {
        Minecraft mc = Minecraft.getMinecraft();
        int fac = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight).getScaleFactor();
        float scale = 1f / fac;
        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, 1.0F);

        if (!mod.getExpansionList().containsKey(ex)) {
            IDontKnowHowToCode.warnOnce("Problem getting expansion " + ex + " to debug");
            return;
        }

        try {
            Exception loadException = mod.getExpansionList().get(ex).getLoadException();

            if (loadException == null) {
                ProviderCollection providers = mod.getExpansionList().get(ex).obtainProvidersForDebugging();
                Distances condition = distances(providers.getCondition());
                Distances junction = distances(providers.getJunction());
                Distances machine = distances(providers.getMachine());

                int yyBase = 30;

                scrub(condition, 20, yyBase);
                scrub(junction, 400, yyBase);
                scrub(machine, 600, yyBase);

                // link(condition, 0, 0, junction, 40, 0);
                // link(junction, 40, 0, machine, 80, 0);
            } else {
                mc.fontRenderer.drawStringWithShadow("There was an error loading the file:", 10, 30, 0xFFFF00);
                mc.fontRenderer.drawStringWithShadow(loadException.getMessage(), 10, 40, 0xFFFF00);
            }
        } catch (Exception e) {
            IDontKnowHowToCode.whoops__printExceptionToChat(mod.getChatter(), e, this);
        }

        GL11.glPopMatrix();
    }

    @SuppressWarnings("unused")
    private void link(Distances reliables, int xR, int yR, Collection<String> dependencies, int xD, int yDapplied,
            boolean right) {
        for (String dependency : dependencies) {
            reliables.get(dependency);

            reliables.visualize(dependency).isActive();
        }
    }

    private void scrub(Distances subject, int x, int y) {
        for (String name : subject.keySet()) {
            Visualized vis = subject.visualize(name);

            paint(x, y + subject.get(name) * GAP, vis);
        }
    }

    private void paint(int x, int y, Visualized vis) {
        String name = vis.getName();
        String feed = vis.getFeed();
        boolean isActive = vis.isActive();

        Minecraft mc = Minecraft.getMinecraft();

        mc.fontRenderer.drawStringWithShadow(name + "(" + feed + ")", x, y, isActive ? 0x0099FF : 0xFF0000);

        // PAINT
    }

    public Distances distances(Provider<? extends Visualized> provider) {
        Distances map = new Distances(provider);

        List<String> list = new ArrayList<>(provider.keySet());
        Collections.sort(list);

        int i = 0;
        for (String name : list) {
            map.put(name, i);
            i = i + 1;
        }

        return map;
    }

    @SuppressWarnings("serial")
    private class Distances extends TreeMap<String, Integer> {
        private Provider<? extends Visualized> provider;

        public Distances(Provider<? extends Visualized> provider) {
            super();
            this.provider = provider;
        }

        public Visualized visualize(String name) {
            return provider.get(name);
        }
    }

}
