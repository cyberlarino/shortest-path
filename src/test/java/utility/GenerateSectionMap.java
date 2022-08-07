package utility;

import shortestpath.worldmap.WorldMapProvider;
import shortestpath.worldmap.sections.SectionMapper;

import java.io.IOException;

public class GenerateSectionMap {
    public static void main(String[] args) throws IOException {
        final WorldMapProvider worldMapProvider = new WorldMapProvider();
        final SectionMapper sectionMapper = new SectionMapper(worldMapProvider);
        sectionMapper.findSections();
        System.out.println("Done mapping sections.\n");
        sectionMapper.toFile();
        System.out.println("Successfully saved to file.");
    }
}
