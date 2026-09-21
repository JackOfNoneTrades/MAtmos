package eu.ha3.matmos.dealias;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public class BlockAliasMap extends ObjectAliasMap {

    @Override
    public String getLogPrefix() {
        return "[BlockAliasMap] ";
    }

    @Override
    public int getIDFromName(String s) {
        return Block.blockRegistry.getIDForObject(Block.blockRegistry.getObject(s));
    }

    @Override
    public int getItemID(Item i) {
        if(i instanceof ItemBlock) {
            return Block.getIdFromBlock(((ItemBlock)i).field_150939_a);
        } else {
            return -1;
        }
    }

    @Override
    public String getNameFromID(int i) {
        return String.valueOf(Block.blockRegistry.getNameForObject(Block.getBlockById(i)));
    }

}
