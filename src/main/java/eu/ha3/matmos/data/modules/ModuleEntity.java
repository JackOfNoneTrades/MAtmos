package eu.ha3.matmos.data.modules;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.ha3.matmos.core.sheet.DataPackage;
import eu.ha3.matmos.data.IDataCollector;
import eu.ha3.matmos.data.IDataGatherer;
import eu.ha3.matmos.util.IDontKnowHowToCode;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;

public class ModuleEntity implements IDataGatherer, PassOnceModule {
    private final Set<String> submodules;
    private final IDataCollector collector;

    private AxisAlignedBB bbox;

    private ModuleProcessor mindistModel;
    private Map<String, Double> minimumDistanceReports;

    private ModuleProcessor[] radiusSheets;
    private int[] radiusValuesSorted;
    private Map<String, Integer>[] entityCount;

    private int maxel;

    private boolean isRequired;

    @SuppressWarnings("unchecked")
    public ModuleEntity(DataPackage dataIn, IDataCollector collector, String minDistModule, String radiModulePrefix,
            int max, int... radiis) {
        this.collector = collector;
        submodules = new LinkedHashSet<>();

        mindistModel = new ModuleProcessor(dataIn, minDistModule) {
            @Override
            protected void doProcess() {
            }
        };
        dataIn.getSheet(minDistModule).setDefaultValue("0");
        submodules.add(minDistModule);
        minimumDistanceReports = new HashMap<>();

        radiusSheets = new ModuleProcessor[radiis.length];
        entityCount = (Map<String, Integer>[]) new Map<?, ?>[radiis.length];

        radiusValuesSorted = Arrays.copyOf(radiis, radiis.length);
        Arrays.sort(radiusValuesSorted);
        maxel = radiusValuesSorted[radiusValuesSorted.length - 1] + 10;

        for (int i = 0; i < radiusValuesSorted.length; i++) {
            int radiNum = radiusValuesSorted[i];
            radiusSheets[i] = new ModuleProcessor(dataIn, radiModulePrefix + radiNum) {
                @Override
                protected void doProcess() {
                }
            };
            dataIn.getSheet(radiModulePrefix + radiNum).setDefaultValue(Integer.toString(Integer.MAX_VALUE));
            submodules.add(radiModulePrefix + radiNum);
            entityCount[i] = new HashMap<>();
        }

        bbox = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
    }

    private void refresh() {
        isRequired = false;
        isRequired = collector.requires(mindistModel.getName());

        for (ModuleProcessor processor : radiusSheets) {
            isRequired = isRequired || collector.requires(processor.getName());
        }

        for (Map<String, Integer> mappy : entityCount) {
            mappy.clear();
        }

        // Reset old things
        for (String type : minimumDistanceReports.keySet()) {
            for (int i = 0; i < radiusValuesSorted.length; i++) {
                radiusSheets[i].setValue(type, 0);
            }

            mindistModel.setValue(type, Integer.MAX_VALUE);
        }
        minimumDistanceReports.clear();
    }

    @Override
    public void process() {
        refresh();

        if (!isRequired) {
            IDontKnowHowToCode.warnOnce("EntityDetector is running but not required. Logic error?");
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();

        double x = mc.thePlayer.posX;
        double y = mc.thePlayer.posY;
        double z = mc.thePlayer.posZ;

        bbox = AxisAlignedBB.getBoundingBox(x - maxel, y - maxel, z - maxel, x + maxel, y + maxel, z + maxel);

        List<Entity> entityList = mc.theWorld.getEntitiesWithinAABB(Entity.class, bbox);

        for (Entity e : entityList) {
            if (e != null && e != mc.thePlayer) {
                double dx = e.posX - x;
                double dy = e.posY - y;
                double dz = e.posZ - z;

                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                if (e instanceof EntityPlayer) {
                    reportDistance("minecraft:player", distance);
                } else {
                    int eID = EntityList.getEntityID(e);

                    if (eID != 0) {
                        reportDistance(String.valueOf(eID), distance);
                    }
                }

                int i = 0;
                boolean reported = false;
                while (i < radiusValuesSorted.length && !reported) {
                    if (distance <= radiusValuesSorted[i]) {
                        // If something is within 1 meter, it certainly also is within 5 meters:
                        // expand now and exit the loop.
                        if (!(e instanceof EntityPlayer)) {
                            int eID = EntityList.getEntityID(e);

                            if (eID != 0) {
                                for (int above = i; above < radiusValuesSorted.length; above++) {
                                    addToEntityCount(above, String.valueOf(eID), 1);
                                }
                            }
                        }
                        reported = true;
                    } else {
                        i++;
                    }
                }
            }
        }

        for (int i = 0; i < radiusValuesSorted.length; i++) {
            if (collector.requires(radiusSheets[i].getName())) {
                for (String entityID : entityCount[i].keySet()) {
                    radiusSheets[i].setValue(entityID, entityCount[i].get(entityID));
                }
            }
        }

        if (collector.requires(mindistModel.getName())) {
            for (String entityID : minimumDistanceReports.keySet()) {
                mindistModel.setValue(entityID, (int) Math.floor(minimumDistanceReports.get(entityID) * 1000));
            }
        }

        // Apply the virtual sheets
        if (collector.requires(mindistModel.getName())) {
            mindistModel.process();
        }

        for (int i = 0; i < radiusValuesSorted.length; i++) {
            if (collector.requires(radiusSheets[i].getName())) {
                radiusSheets[i].process();
            }
        }
    }

    protected void addToEntityCount(int radiIndex, String entityID, int count) {
        if (entityCount[radiIndex].containsKey(entityID)) {
            entityCount[radiIndex].put(entityID, entityCount[radiIndex].get(entityID) + count);
        } else {
            entityCount[radiIndex].put(entityID, count);
        }
    }

    protected void reportDistance(String type, double distance) {
        if (!minimumDistanceReports.containsKey(type) || minimumDistanceReports.get(type) > distance) {
            minimumDistanceReports.put(type, distance);
        }
    }

    @Override
    public String getName() {
        return "_POM__entity_detector";
    }

    @Override
    public Set<String> getSubModules() {
        return submodules;
    }

}
