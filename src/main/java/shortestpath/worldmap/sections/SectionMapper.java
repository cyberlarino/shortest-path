package shortestpath.worldmap.sections;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.runelite.api.coords.WorldPoint;
import shortestpath.pathfinder.Node;
import shortestpath.pathfinder.NodeGraph;
import shortestpath.pathfinder.path.Movement;
import shortestpath.pathfinder.path.Transport;
import shortestpath.worldmap.WorldMapProvider;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.google.gson.Gson;

public class SectionMapper {
    private static final Path DEFAULT_SECTION_MAP_ZIP_PATH = Paths.get("src/main/resources/section-map.zip");
    private static final ZipEntry DEFAULT_SECTION_MAP_ZIP_ENTRY = new ZipEntry("section-map.json");

    private final WorldMapProvider worldMapProvider;
    private List<Set<WorldPoint>> sections = new ArrayList<>();
    private Map<Movement, MovementSections> movementSectionsMap = new HashMap<>();

    public SectionMapper(final WorldMapProvider worldMapProvider) {
        this.worldMapProvider = worldMapProvider;
    }

    public void findSections() {
        for (final Transport transport : worldMapProvider.getWorldMap().getTransports()) {
            floodFill(transport.getOrigin());
            floodFill(transport.getDestination());
        }
    }

    @Nullable
    public Integer getSectionId(final WorldPoint point) {
        for (int i = 0; i < sections.size(); ++i) {
            if (sections.get(i).contains(point)) {
                return i;
            }
        }
        return null;
    }

    public MovementSections getSectionId(final Movement movement) {
        MovementSections movementSections = movementSectionsMap.get(movement);
        if (movementSections == null) {
            final Integer originSection = getSectionId(movement.getOrigin());
            final Integer destinationSection = getSectionId(movement.getDestination());
            movementSections = new MovementSections(originSection, destinationSection);
            movementSectionsMap.put(movement, movementSections);
        }
        return movementSections;
    }

    private void floodFill(final WorldPoint point) {
        if (getSectionId(point) != null) {
            return;
        }

        final NodeGraph graph = new NodeGraph(worldMapProvider.getWorldMap());
        graph.addBoundaryNode(Node.createInitialNode(point));

        while (!graph.getBoundary().isEmpty()) {
            graph.evaluateBoundaryNode(0, (x) -> true, (x) -> false);
        }

        sections.add(graph.getVisited());
        if (graph.getVisited().isEmpty()) {
            Set<WorldPoint> temporarySet = new HashSet<>();
            temporarySet.add(point);
            sections.add(temporarySet);
        }
    }

    public void saveSectionsToFile() throws IOException {
        saveSectionsToFile(DEFAULT_SECTION_MAP_ZIP_PATH, DEFAULT_SECTION_MAP_ZIP_ENTRY);
    }

    public void saveSectionsToFile(final Path filepath, final ZipEntry zipEntry) throws IOException {
        Gson gson = new Gson();
        Type sectionType = new TypeToken<HashSet<WorldPoint>>(){}.getType();
        ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(filepath));
        out.putNextEntry(zipEntry);
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        writer.setIndent("  ");
        writer.beginArray();
        for (Set<WorldPoint> section : sections) {
            gson.toJson(section, sectionType, writer);
        }
        writer.endArray();
        writer.close();
    }

    public static SectionMapper fromFile(final WorldMapProvider worldMapProvider) {
        return SectionMapper.fromFile(DEFAULT_SECTION_MAP_ZIP_PATH, worldMapProvider);
    }

    public static SectionMapper fromFile(final Path filepath, final WorldMapProvider worldMapProvider) {
        final Gson gson = new Gson();
        final Type sectionType = new TypeToken<HashSet<WorldPoint>>(){}.getType();
        final List<Set<WorldPoint>> sections = new ArrayList<>();
        try {
            ZipInputStream in = new ZipInputStream(Files.newInputStream(filepath));
            in.getNextEntry();
            JsonReader reader = new JsonReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            reader.beginArray();
            while (reader.hasNext()) {
                final Set<WorldPoint> section = gson.fromJson(reader, sectionType);
                sections.add(section);
            }
            reader.endArray();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SectionMapper sectionMapper = new SectionMapper(worldMapProvider);
        sectionMapper.sections = sections;
        return sectionMapper;
    }
}
