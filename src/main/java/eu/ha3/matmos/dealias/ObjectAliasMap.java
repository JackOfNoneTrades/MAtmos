package eu.ha3.matmos.dealias;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import eu.ha3.matmos.ConfigManager;
import eu.ha3.matmos.Matmos;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public abstract class ObjectAliasMap {
    
    protected Map<Integer, Integer> dealiasMap = new HashMap<>();
    
    public void addMappings(List<AliasEntry> entries){
        entries.forEach(e -> {
            String k = e.getKey();
            String v = e.getValue();
            int ki = getIDFromName(k);
            if(ki > 0) {
                if(v.startsWith(":")) {
                    String oreName = v.substring(1);
                    List<ItemStack> ores = OreDictionary.getOres(oreName);
                    if(!ores.isEmpty()) {
                        for(ItemStack is : ores) {
                            int id = getItemID(is.getItem());
                            if(id > 0) {
                                dealiasMap.put(id, ki);
                            }
                        }
                    } else {
                        e.warn("Ignoring invalid oredict name in alias map: " + v);
                    }
                } else {
                    if(v.contains("*")) {
                        try {
                            Pattern pattern = makePattern(v);
                            boolean matchedName = false;
                            for(Object o : Item.itemRegistry.getKeys()){
                                String name = String.valueOf(o);
                                if(pattern.matcher(name).matches() ||
                                        (name.startsWith("minecraft:") && pattern.matcher(name.substring("minecraft:".length())).matches())) {
                                    matchedName = true;
                                    int vi = getIDFromName(name);
                                    if(vi > 0) {
                                        dealiasMap.put(vi, ki);
                                    }
                                }
                            }
                            if(!matchedName) {
                                e.warn("No name matched pattern " + v);
                            }
                        } catch (Exception e2) {
                            e.warn("Invalid pattern: " + v + " (" + e2 + ")");
                        }
                    } else {
                        int vi = getIDFromName(v);
                        if(vi > 0) {
                            dealiasMap.put(vi, ki);
                        } else if(!isValidName(v)){
                            e.warn("Ignoring invalid name in alias map: " + v);
                        }
                    }
                }
            } else if(!isValidName(k)) {
                e.warn("Ignoring invalid name in alias map: " + k);
            }
        });
    }
    
    public boolean isValidName(String name) {
        return Item.itemRegistry.containsKey(name) || Block.blockRegistry.containsKey(name);
    }
    
    public void dealiasItemGroupToMinID(Collection<String> names) {
        List<Integer> ids = names.stream()
                .filter(s -> !s.startsWith("minecraft:"))
                .mapToInt(s -> getIDFromName(s)).filter(i -> i != -1).boxed().collect(Collectors.toList());
        
        int minBlockID = ids.stream().min(Integer::compare).orElse(-1);
        
        Matmos.ALIASLOGGER.debug(getLogPrefix() + "filtered IDs: " + Arrays.toString(ids.toArray()));
        Matmos.ALIASLOGGER.debug(getLogPrefix() + "min: " + getNameFromID(minBlockID) + " (" + minBlockID + ")");
        
        if(minBlockID != -1) {
            ids.forEach(i -> {if(!dealiasMap.containsKey(i)) dealiasMap.put(i, minBlockID);});
        }
    }
    
    Pattern makePattern(String str) throws Exception {
        str = str.replace(".", "\\.").replace("*", ".*");
        
        Pattern pattern = null;
        try {
            pattern = Pattern.compile(str);
        } catch (PatternSyntaxException e) {
            throw e;
        }
        return pattern;
    }
    
    protected void compile() {
        dealiasMap.entrySet().removeIf(e -> {
            Integer i = e.getKey();
            int id = i;
            int hops = 0;
            do {
                id = dealiasMap.get(id);
                hops++;
            } while (dealiasMap.containsKey(id) && dealiasMap.get(id) != id);

            if (id == i && hops > 1) {
                Matmos.LOGGER.warn("Circular dependency detected when dealiasing "
                        + getNameFromID(i) + ". Alias will be ignored.");
                return true;
            } else { // OK
                e.setValue(id);
                return false;
            }
        });
    }
    
    public int dealiasID(int alias) {
        return dealiasMap.getOrDefault(alias, alias);
    }
    
    public abstract String getLogPrefix();
    
    public abstract int getIDFromName(String s);
    
    public abstract int getItemID(Item i);
    
    public abstract String getNameFromID(int i);
    
}
