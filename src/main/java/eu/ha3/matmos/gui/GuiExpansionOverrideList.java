package eu.ha3.matmos.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Sets;

import eu.ha3.matmos.core.Provider;
import eu.ha3.matmos.core.expansion.Expansion;
import eu.ha3.matmos.core.logic.Condition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import eu.ha3.mc.abstraction.util.ATextFormatting;

public class GuiExpansionOverrideList extends GuiListExtended {
    
    private final Expansion expansion;
    private final List<OverrideEntry> listEntries = new ArrayList<>();
    
    private final Map<String, String> overrides;
    private final Provider<Condition> conditionProvider;
    private final Minecraft mc;
    private int padding = 16;
    
    public GuiExpansionOverrideList(Minecraft mc, GuiExpansionOverrides eo, Expansion expansion) {
        super(mc, eo.width, eo.height, 0, eo.height - 20, 8);
        this.expansion = expansion;
        this.overrides = expansion.getConditionValueOverrides();
        this.mc = mc;
        
        conditionProvider = expansion.obtainProvidersForDebugging().getCondition();
        Set<String> conditions = conditionProvider.keySet().stream().filter(s -> !s.startsWith("_")).collect(Collectors.toSet());
        if(!conditions.isEmpty()) {
            Collection<String> posConditions = conditions.stream()
                    .filter(c -> conditionProvider.get(c).getIndex().getSheet().equals("cb_pos"))
                    .collect(Collectors.toList());
            Collection<String> otherConditions = Sets.difference(conditions, new HashSet<>(posConditions))
                    .stream().collect(Collectors.toList());
            
            listEntries.add(new OverrideEntry(ATextFormatting.BOLD + I18n.format("mat.title.conditionoverrides") + ATextFormatting.RESET +
                    ATextFormatting.GRAY + " (" + expansion.getName() + ".cfg)", true));
            if(!posConditions.isEmpty()) {
                listEntries.add(new OverrideEntry(I18n.format("mat.options.overridesection.pos"), true));
                posConditions.forEach(k -> listEntries.add(new OverrideEntry(k)));
                listEntries.add(new OverrideEntry("", true));
            }
            if(!otherConditions.isEmpty()) {
                listEntries.add(new OverrideEntry(I18n.format("mat.options.overridesection.other"), true));
                otherConditions.forEach(k -> listEntries.add(new OverrideEntry(k)));
            }
        } else {
            listEntries.add(new OverrideEntry(I18n.format("mat.options.overridesection.nothing"), true));
        }
    }

    public void onGuiClosed() {
        for(OverrideEntry oe : listEntries) {
            if(oe.tf != null) {
                String text = oe.getText();
                if(!text.equals("")){
                    overrides.put(oe.s, text);
                } else {
                    overrides.remove(oe.s);
                }
            }
        }
    }
    
    @Override
    public IGuiListEntry getListEntry(int index) {
        return listEntries.get(index);
    }

    @Override
    protected int getSize() {
        return listEntries.size();
    }
    
    public void mouseClicked(int x, int y, int buttonClicked) {
        listEntries.forEach(l -> l.mouseClicked(x, y, buttonClicked));
    }
    
    public void keyTyped(char ch, int i) {
        listEntries.forEach(l -> l.keyTyped(ch, i));
    }
    
    void updateScreen() {
        listEntries.forEach(l -> l.updateScreen());
    }
    
    @Override
    public int getListWidth() {
        return this.width;
    }
    
    @Override
    protected int getScrollBarX() {
        return this.width - padding;
    }
    
    public class OverrideEntry implements GuiListExtended.IGuiListEntry {
        
        private String s;
        
        private GuiTextField tf;
        
        public OverrideEntry(String s, boolean sectionHeader) {
            this.s = s;
            
            if(!sectionHeader) {
                tf = new GuiTextField(mc.fontRenderer, 100, 0, 50 * 2, 10);
                tf.setText(expansion.getConditionValueOverrides().getOrDefault(s, ""));    
            }
        }
        
        public OverrideEntry(String s) {
            this(s, false);
        }
        
        @Override
        public void drawEntry(int index, int x, int y, int p_148279_4_, int p_148279_5_,
                Tessellator p_148279_6_, int p_148279_7_, int p_148279_8_, boolean p_148279_9_) {
            GL11.glPushMatrix();
            int fac = 2;
            float scale = 1f / fac;
            GL11.glScalef(scale, scale, 1.0F);
            Minecraft mc = Minecraft.getMinecraft();
            if(tf != null) {
                if(index % 2 == 0) {
                    Gui.drawRect((x + padding) * fac, (y - 1) * fac, (width - padding) * fac, (y + 4 + 2) * fac, 0x20FFFFFF);
                }
                Condition cond = conditionProvider.get(s);
                boolean overridden = !tf.getText().isEmpty();
                mc.fontRenderer.drawString((overridden ? (ATextFormatting.GOLD + "" + ATextFormatting.BOLD) : ATextFormatting.WHITE) + cond.getName() + "  " + ATextFormatting.GRAY + cond.getFeed(), (x + padding) * fac, y * fac, 0xFFFFFF);
                tf.xPosition = (width - padding - 4 - padding - 50) * fac;
                tf.yPosition = y * fac;
                tf.drawTextBox();
            } else {
                mc.fontRenderer.drawString(s, (width / 2 - mc.fontRenderer.getStringWidth(s) / 4) * fac, y * fac, 0xFFFFFF);
            }
            GL11.glPopMatrix();
        }
        
        public String getText() {
            return tf == null ? "" : tf.getText();
        }
        
        public void mouseClicked(int x, int y, int buttonClicked) {
            if(tf != null) {
                tf.mouseClicked(x * 2, y * 2, buttonClicked);
            }
        }
        
        public void keyTyped(char ch, int i) {
            if(tf != null) {
                tf.textboxKeyTyped(ch, i);
            }
        }
        
        public void updateScreen() {
            if(tf != null) {
                tf.updateCursorCounter();
            }
        }
        
        @Override
        public boolean mousePressed(int p_148278_1_, int p_148278_2_, int p_148278_3_, int p_148278_4_, int p_148278_5_,
                int p_148278_6_) { return false; }

        @Override
        public void mouseReleased(int p_148277_1_, int p_148277_2_, int p_148277_3_, int p_148277_4_, int p_148277_5_,
                int p_148277_6_) { }
    }
}
