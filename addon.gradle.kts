import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.SourceSetContainer

extensions.configure<SourceSetContainer> {
    named("main") {
        java {
            setSrcDirs(listOf("src/main/java", "projects/common/src/common/java", "projects/1.7/lib/MC-Commons/mc-src"))
            exclude("eu/ha3/matmos/LiteModMAtmos.java")
            exclude("eu/ha3/mc/haddon/litemod/**")
        }
        resources {
            setSrcDirs(listOf("src/main/resources"))
            exclude("litemod.json")
            exclude("haddon.mixin.json")
        }
    }
}

tasks.named<ShadowJar>("shadowJar") {
    relocate("eu.ha3.easy", "eu.ha3.matmos.lib.eu.ha3.easy")
    relocate("eu.ha3.mc", "eu.ha3.matmos.lib.eu.ha3.mc")
    relocate("eu.ha3.util", "eu.ha3.matmos.lib.eu.ha3.util")
    relocate("net.sf.practicalxml", "eu.ha3.matmos.lib.net.sf.practicalxml")
    relocate("net.sf.kdgcommons", "eu.ha3.matmos.lib.net.sf.kdgcommons")
}
