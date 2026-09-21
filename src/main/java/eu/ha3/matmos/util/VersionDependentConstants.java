package eu.ha3.matmos.util;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public class VersionDependentConstants {
    
    public static final String RAIN_BLACKLIST = "ambient.weather.rain,ambient.weather.rain_calm,rain";
    
    public static final List<Pair<String, String>> SOUND_SYSTEM_REPLACER_CONFLICTS = Arrays.asList(Pair.of("Dynamic Surroundings", "org/blockartistry/mod/DynSurround")); 
}
