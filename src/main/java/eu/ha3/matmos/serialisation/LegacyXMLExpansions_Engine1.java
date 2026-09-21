package eu.ha3.matmos.serialisation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import eu.ha3.matmos.core.Dynamic;
import eu.ha3.matmos.core.Operator;
import eu.ha3.matmos.core.sheet.SheetEntry;
import eu.ha3.matmos.core.sheet.SheetIndex;
import eu.ha3.matmos.serialisation.expansion.SerialCondition;
import eu.ha3.matmos.serialisation.expansion.SerialDynamic;
import eu.ha3.matmos.serialisation.expansion.SerialDynamicSheetIndex;
import eu.ha3.matmos.serialisation.expansion.SerialEvent;
import eu.ha3.matmos.serialisation.expansion.SerialList;
import eu.ha3.matmos.serialisation.expansion.SerialMachine;
import eu.ha3.matmos.serialisation.expansion.SerialMachineEvent;
import eu.ha3.matmos.serialisation.expansion.SerialMachineStream;
import eu.ha3.matmos.serialisation.expansion.SerialRoot;
import eu.ha3.matmos.serialisation.expansion.SerialSet;
import eu.ha3.matmos.util.MAtUtil;
import eu.ha3.matmos.util.math.Numbers;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.sf.practicalxml.DomUtil;

@Deprecated
public class LegacyXMLExpansions_Engine1 {
    private static final String AS_ITEM = "__AS_ITEM";
    private static final String AS_BLOCK = "__AS_BLOCK";
    private static final String NAME = "name";
    private static final String LIST = "list";

    private static final String CONDITION = "condition";
    private static final String SHEET = "sheet";
    private static final String KEY = "key";
    private static final String DYNAMICKEY = "dynamickey";
    private static final String SYMBOL = "symbol";
    private static final String CONSTANT = "constant";

    private static final String SET = "set";
    private static final String TRUEPART = "truepart";
    private static final String FALSEPART = "falsepart";

    private static final String EVENT = "event";
    private static final String VOLMIN = "volmin";
    private static final String VOLMAX = "volmax";
    private static final String PITCHMIN = "pitchmin";
    private static final String PITCHMAX = "pitchmax";
    private static final String METASOUND = "metasound";
    private static final String PATH = "path";

    private static final String MACHINE = "machine";
    private static final String ALLOW = "allow";
    private static final String RESTRICT = "restrict";

    private static final String DYNAMIC = "dynamic";
    private static final String ENTRY = "entry";

    private static final String EVENTTIMED = "eventtimed";
    private static final String EVENTNAME = "eventname";
    private static final String VOLMOD = "volmod";
    private static final String PITCHMOD = "pitchmod";
    private static final String DELAYSTART = "delaystart";
    private static final String DELAYMIN = "delaymin";
    private static final String DELAYMAX = "delaymax";

    private static final String STREAM = "stream";
    // PATH already covered
    private static final String VOLUME = "volume";
    private static final String PITCH = "pitch";
    private static final String FADEINTIME = "fadeintime";
    private static final String FADEOUTTIME = "fadeouttime";
    private static final String DELAYBEFOREFADEIN = "delaybeforefadein";
    private static final String DELAYBEFOREFADEOUT = "delaybeforefadeout";
    private static final String ISLOOPING = "islooping";
    private static final String ISUSINGPAUSE = "isusingpause";

    private Map<String, String> scanDicts = new HashMap<>();

    private SerialRoot root;

    private Map<String, DynamicElementSerialiser> serialisers = new HashMap<>();

    @FunctionalInterface
    protected interface DynamicElementSerialiser {
        void parseXML(Element capsule, String name);
    }

    public LegacyXMLExpansions_Engine1() {
        serialisers.put(DYNAMIC, this::parseXML_1_dynamic);
        serialisers.put(LIST, this::parseXML_2_list);
        serialisers.put(CONDITION, this::parseXML_3_condition);
        serialisers.put(SET, this::parseXML_4_set);
        serialisers.put(EVENT, this::parseXML_5_event);
        serialisers.put(MACHINE, this::parseXML_6_machine);

        scanDicts.put("LargeScan", "scan_large");
        scanDicts.put("LargeScanPerMil", "scan_large_p1k");
        scanDicts.put("SmallScan", "scan_small");
        scanDicts.put("SmallScanPerMil", "scan_small_p1k");
        scanDicts.put("ContactScan", "scan_contact");
    }

    public SerialRoot loadXMLtoSerial(Document doc) {
        root = new SerialRoot();

        try {
            parseXMLtoSerial(doc);
            return root;
        } catch (Exception e) {
            e.printStackTrace();
            return new SerialRoot();
        }
    }

    private String asBlock(int il) {
        Block block = Block.getBlockById(il);
        if (block == null) {
            return null;
        }

        return MAtUtil.nameOf(block);
    }

    private String asBlock(String longFloatSimplificated) {
        if (longFloatSimplificated == null) {
            return null;
        }

        Long l = Numbers.toLong(longFloatSimplificated);
        if (l == null) {
            return null;
        }

        return asBlock((int) (long) l);
    }

    private String asItem(int il) {
        Item item = Item.getItemById(il);
        if (item == null) {
            return null;
        }

        return MAtUtil.nameOf(item);
    }

    private String asItem(String longFloatSimplificated) {
        if (longFloatSimplificated == null) {
            return null;
        }

        Long l = Numbers.toLong(longFloatSimplificated);
        if (l == null) {
            return null;
        }

        return asItem((int) (long) l);
    }

    private String eltString(String tagName, Element ele) {
        return textOf(DomUtil.getChild(ele, tagName));
    }

    private SerialMachineEvent inscriptXMLeventTimed(Element specs) {
        SerialMachineEvent sme = new SerialMachineEvent();
        sme.event = eltString(EVENTNAME, specs);
        sme.vol_mod = toFloat(eltString(VOLMOD, specs));
        sme.pitch_mod = toFloat(eltString(PITCHMOD, specs));
        sme.delay_min = toFloat(eltString(DELAYMIN, specs));
        sme.delay_max = toFloat(eltString(DELAYMAX, specs));
        sme.delay_start = toFloat(eltString(DELAYSTART, specs));

        return sme;
    }

    private float toFloat(String value) {
        return value != null ? Float.parseFloat(value) : 1;
    }

    private SerialMachineStream inscriptXMLstream(Element specs, SerialMachine machine) {
        SerialMachineStream sms = new SerialMachineStream();

        sms.path = eltString(PATH, specs);
        sms.vol = toFloat(eltString(VOLUME, specs));
        sms.pitch = toFloat(eltString(PITCH, specs));
        machine.fadein = toFloat(eltString(FADEINTIME, specs));
        machine.fadeout = toFloat(eltString(FADEOUTTIME, specs));
        machine.delay_fadein = toFloat(eltString(DELAYBEFOREFADEIN, specs));
        machine.delay_fadeout = toFloat(eltString(DELAYBEFOREFADEOUT, specs));
        sms.looping = toInt(eltString(ISLOOPING, specs)) == 1;
        sms.pause = toInt(eltString(ISUSINGPAUSE, specs)) == 1;

        return sms;
    }

    private String nameOf(Element element) {
        if (element == null) {
            return null;
        }

        Node nameNode = element.getAttributes().getNamedItem(NAME);
        if (nameNode == null) {
            return null;
        }

        return nameNode.getNodeValue();
    }

    private void parseXMLtoSerial(Document doc) throws XPathExpressionException, DOMException {
        Element elt = doc.getDocumentElement();
        DomUtil.removeEmptyTextRecursive(elt);

        for (Entry<String, DynamicElementSerialiser> entry : serialisers.entrySet()) {
            for (Element capsule : DomUtil.getChildren(elt, entry.getKey())) {
                String name = nameOf(capsule);

                if (name != null) {
                    entry.getValue().parseXML(capsule, name);
                }
            }
        }
    }

    private void parseXML_1_dynamic(Element capsule, String name) {
        SerialDynamic dynamic = new SerialDynamic();

        for (Element eelt : DomUtil.getChildren(capsule, ENTRY)) {
            SerialDynamicSheetIndex sdsi = new SerialDynamicSheetIndex();

            String sheet = eelt.getAttributes().getNamedItem(SHEET).getNodeValue();
            String index = textOf(eelt);

            if (sheet.contains("Scan")) {
                index = recomputeBlockName(index);
                sheet = recomputeScanSheetName(sheet);
            }

            SheetIndex si = new LegacySheetIndex_Engine0to1(sheet, index);

            sdsi.sheet = si.getSheet();
            sdsi.index = si.getIndex();
            dynamic.entries.add(sdsi);
        }

        root.dynamic.put(name, dynamic);
    }

    private void parseXML_2_list(Element capsule, String name) {
        SerialList list = new SerialList();
        SerialList asItem = new SerialList();
        SerialList asBlock = new SerialList();

        for (Element eelt : DomUtil.getChildren(capsule, CONSTANT)) {
            list.entries.add(textOf(eelt));

            Long l = Numbers.toLong(textOf(eelt));
            if (l != null) {
                int il = (int) (long) l;
                if (asBlock(il) != null) {
                    asBlock.entries.add(asBlock(il));
                }
                if (asItem(il) != null) {
                    asItem.entries.add(asItem(il));
                }
            }
        }
        root.list.put(name, list);
        root.list.put(name + AS_BLOCK, asBlock);
        root.list.put(name + AS_ITEM, asItem);
    }

    private void parseXML_3_condition(Element capsule, String name) {
        String sheetNotComputed = eltString(SHEET, capsule);
        String indexNotComputed = eltString(KEY, capsule);
        String dynamicIndexXX = eltString(DYNAMICKEY, capsule);
        String symbol = eltString(SYMBOL, capsule);
        String value = eltString(CONSTANT, capsule);
        String listValueXX = eltString(LIST, capsule);

        boolean dynamic = false;

        if (dynamicIndexXX != null) {
            sheetNotComputed = Dynamic.DEDICATED_SHEET;
            indexNotComputed = dynamicIndexXX;
            dynamic = true;
        }
        if (listValueXX != null) {
            value = listValueXX;
        }

        if (sheetNotComputed.contains("Scan")) {
            indexNotComputed = recomputeBlockName(indexNotComputed);
            sheetNotComputed = recomputeScanSheetName(sheetNotComputed);
        }

        SheetIndex si = !dynamic ? new LegacySheetIndex_Engine0to1(sheetNotComputed, indexNotComputed)
                : new SheetEntry(sheetNotComputed, indexNotComputed);

        root.condition.put(name, new SerialCondition(si, Operator.fromSymbol(symbol).getSerializedForm(), value));

        if (si instanceof LegacySheetIndex_Engine0to1) {
            if (((LegacySheetIndex_Engine0to1) si).isBlock() && asBlock(value) != null) {
                root.condition.put(name + AS_BLOCK,
                        new SerialCondition(si, Operator.fromSymbol(symbol).getSerializedForm(), asBlock(value)));
            }

            if (((LegacySheetIndex_Engine0to1) si).isItem() && asItem(value) != null) {
                root.condition.put(name + AS_ITEM,
                        new SerialCondition(si, Operator.fromSymbol(symbol).getSerializedForm(), asItem(value)));
            }
        }
    }

    private void parseXML_4_set(Element capsule, String name) {
        SerialSet set = new SerialSet();

        for (Element eelt : DomUtil.getChildren(capsule, TRUEPART)) {
            set.yes.add(textOf(eelt));
        }

        for (Element eelt : DomUtil.getChildren(capsule, FALSEPART)) {
            set.no.add(textOf(eelt));
        }

        root.set.put(name, set);
    }

    private void parseXML_5_event(Element capsule, String name) {
        SerialEvent event = new SerialEvent();

        String volmin = eltString(VOLMIN, capsule);
        String volmax = eltString(VOLMAX, capsule);
        String pitchmin = eltString(PITCHMIN, capsule);
        String pitchmax = eltString(PITCHMAX, capsule);
        String metasound = eltString(METASOUND, capsule);

        event.vol_min = volmin != null ? Float.parseFloat(volmin) : 1f;
        event.vol_max = volmax != null ? Float.parseFloat(volmax) : 1f;
        event.pitch_min = pitchmin != null ? Float.parseFloat(pitchmin) : 1f;
        event.pitch_max = pitchmax != null ? Float.parseFloat(pitchmax) : 1f;
        event.distance = metasound != null ? toInt(metasound) : 0;

        for (Element eelt : DomUtil.getChildren(capsule, PATH)) {
            event.path.add(textOf(eelt));
        }

        root.event.put(name, event);
    }

    private void parseXML_6_machine(Element capsule, String name) {
        SerialMachine machine = new SerialMachine();

        SerialMachineStream stream = null;
        for (Element eelt : DomUtil.getChildren(capsule, STREAM)) {
            stream = inscriptXMLstream(eelt, machine);
        }
        if (stream != null) {
            ArrayList<SerialMachineStream> streamList = new ArrayList<SerialMachineStream>();
            streamList.add(stream);
            machine.stream = streamList;
        } else {
            machine.delay_fadein = 0f;
            machine.delay_fadeout = 0f;
            machine.fadein = 0f;
            machine.fadeout = 0f;
        }

        for (Element eelt : DomUtil.getChildren(capsule, EVENTTIMED)) {
            machine.event.add(inscriptXMLeventTimed(eelt));
        }

        for (Element eelt : DomUtil.getChildren(capsule, ALLOW)) {
            machine.allow.add(textOf(eelt));
        }

        for (Element eelt : DomUtil.getChildren(capsule, RESTRICT)) {
            machine.restrict.add(textOf(eelt));
        }

        root.machine.put(name, machine);
    }

    private String recomputeBlockName(String index) {
        Long l = Numbers.toLong(index);
        if (l != null && l < 256) {
            Object o = Block.blockRegistry.getObjectById((int) (long) l);
            if (o != null && o instanceof Block) {
                index = MAtUtil.nameOf((Block) o);
            } else {
                System.err.println("??? Failed to convert block with index " + index);
            }
        } else {
            System.err.println("??? Failed to convert block with index " + index + " out of bounds?");
        }

        return index;
    }

    private String recomputeScanSheetName(String sheet) {
        if (scanDicts.containsKey(sheet)) {
            return scanDicts.get(sheet);
        }

        System.err.println("Scan sheet has no equivalent: " + scanDicts);
        return sheet;

    }

    private String textOf(Element ele) {
        if (ele == null || ele.getFirstChild() == null) {
            return null;
        }

        return ele.getFirstChild().getNodeValue();
    }

    private int toInt(String source) {
        try {
            return Integer.parseInt(source);
        } catch (NumberFormatException e) {
            return (int) Float.parseFloat(source);
        }
    }
}
