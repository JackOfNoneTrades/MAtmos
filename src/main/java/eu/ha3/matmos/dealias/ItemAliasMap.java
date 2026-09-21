package eu.ha3.matmos.dealias;

import java.util.Arrays;
import java.util.Set;

import eu.ha3.matmos.ConfigManager;
import eu.ha3.matmos.Matmos;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

public class ItemAliasMap extends ObjectAliasMap {
    
    protected void guessAliases() {
        int swordID = Item.itemRegistry.getIDForObject(Item.itemRegistry.getObject("iron_sword"));
        int wswordID = Item.itemRegistry.getIDForObject(Item.itemRegistry.getObject("wooden_sword"));
        int pickID = Item.itemRegistry.getIDForObject(Item.itemRegistry.getObject("iron_pickaxe"));
        int wpickID = Item.itemRegistry.getIDForObject(Item.itemRegistry.getObject("wooden_pickaxe"));
        int shovelID = Item.itemRegistry.getIDForObject(Item.itemRegistry.getObject("iron_shovel"));
        int wshovelID = Item.itemRegistry.getIDForObject(Item.itemRegistry.getObject("wooden_shovel"));
        int axeID = Item.itemRegistry.getIDForObject(Item.itemRegistry.getObject("iron_axe"));
        int waxeID = Item.itemRegistry.getIDForObject(Item.itemRegistry.getObject("wooden_axe"));
        int hoeID = Item.itemRegistry.getIDForObject(Item.itemRegistry.getObject("iron_hoe"));
        int whoeID = Item.itemRegistry.getIDForObject(Item.itemRegistry.getObject("wooden_hoe"));
        int bowID = Item.itemRegistry.getIDForObject(Item.itemRegistry.getObject("bow"));
        int appleID = Item.itemRegistry.getIDForObject(Item.itemRegistry.getObject("apple"));
        
        for(Object o : Item.itemRegistry) {
            if(!(o instanceof Item)) continue;
            
            int id = Item.itemRegistry.getIDForObject((Item)o);
            String name = Item.itemRegistry.getNameForObject((Item)o).toString();
            if(name.startsWith("minecraft:")) {
                continue;
            }
            
            boolean wooden = name.toLowerCase().contains("wood");
            int target = -1;
            
            Set<String> toolClasses = ((Item)o).getToolClasses(new ItemStack((Item)o));
            Matmos.ALIASLOGGER.debug("Tool classes for " + name + " (" + id + "): " + Arrays.toString(toolClasses.toArray()));
            
            if(o instanceof ItemSword || toolClasses.contains("sword")) {
                target = wooden ? wswordID : swordID;
            } else if(o instanceof ItemPickaxe || toolClasses.contains("pickaxe")) {
                target = wooden ? wpickID : pickID;
            } else if(o instanceof ItemSpade || toolClasses.contains("shovel")) {
                target = wooden ? wshovelID : shovelID;
            } else if(o instanceof ItemAxe || toolClasses.contains("axe")) {
                target = wooden ? waxeID : axeID;
            } else if(o instanceof ItemHoe || toolClasses.contains("hoe")) {
                target = wooden ? whoeID : hoeID;
            } else if(o instanceof ItemBow || toolClasses.contains("bow")) {
                target = bowID;
            } else if(((Item)o).getItemUseAction(new ItemStack((Item)o)) == EnumAction.eat) {
                target = appleID;
            }
            
            if(target != -1) {
                Matmos.ALIASLOGGER.debug("Guessing alias " + 
                    Item.itemRegistry.getNameForObject(Item.itemRegistry.getObjectById(target)) + 
                    " for " + name);
                dealiasMap.put(id, target);
            }
        }
    }

    @Override
    public String getLogPrefix() {
        return "[ItemAliasMap] ";
    }

    @Override
    public int getIDFromName(String s) {
        return Item.itemRegistry.getIDForObject(Item.itemRegistry.getObject(s));
    }

    @Override
    public int getItemID(Item i) {
        return Item.getIdFromItem(i);
    }

    @Override
    public String getNameFromID(int i) {
        return String.valueOf(Item.itemRegistry.getNameForObject(Item.itemRegistry.getObjectById(i)));
    }

}
