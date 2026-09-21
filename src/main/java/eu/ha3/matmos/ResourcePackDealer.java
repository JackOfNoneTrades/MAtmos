package eu.ha3.matmos;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.ResourceLocation;

public class ResourcePackDealer {
    private static final ResourceLocation mat_pack = new ResourceLocation("matmos", "mat_pack.json");
    private static final ResourceLocation expansions = new ResourceLocation("matmos", "expansions.json");

    public Stream<IResourcePack> findResourcePacks() {
        return ((Stream<IResourcePack>)Stream
                .concat(((List<ResourcePackRepository.Entry>) (Minecraft.getMinecraft().getResourcePackRepository()
                        .getRepositoryEntries())).stream().map(ResourcePackRepository.Entry::getResourcePack),
                        Minecraft.getMinecraft().defaultResourcePacks.stream()))
                .filter(this::checkCompatible).distinct();
    }

    public List<IResourcePack> findDisabledResourcePacks() {
        ResourcePackRepository rrr = Minecraft.getMinecraft().getResourcePackRepository();

        List<ResourcePackRepository.Entry> repo = new ArrayList<>(rrr.getRepositoryEntriesAll());
        repo.removeAll(rrr.getRepositoryEntries());

        return repo.stream().map(ResourcePackRepository.Entry::getResourcePack).filter(this::checkCompatible)
                .collect(Collectors.toList());
    }

    private boolean checkCompatible(IResourcePack pack) {
        return pack.resourceExists(mat_pack);
    }

    public InputStream openExpansionsPointerFile(IResourcePack pack) throws IOException {
        return pack.getInputStream(expansions);
    }

    public InputStream openMatPackPointerFile(IResourcePack pack) throws IOException {
        return pack.getInputStream(mat_pack);
    }
}
