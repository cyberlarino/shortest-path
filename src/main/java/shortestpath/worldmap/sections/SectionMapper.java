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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
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
    private static final ZipEntry SECTIONS_ZIP_ENTRY = new ZipEntry("section-map.json");

    private List<Set<WorldPoint>> sections = new ArrayList<>();
    private final WorldMapProvider worldMapProvider;
    private final Map<Movement, MovementSections> movementSectionsMap = new HashMap<>();

    public SectionMapper(final WorldMapProvider worldMapProvider) {
        this.worldMapProvider = worldMapProvider;
    }

    public void findSections() {
        sections = new ArrayList<>();
        for (final Transport transport : worldMapProvider.getWorldMap().getTransports()) {
            floodFill(transport.getOrigin());
            floodFill(transport.getDestination());
        }
    }

    @Nullable
    public Integer getSection(final WorldPoint point) {
        for (int i = 0; i < sections.size(); ++i) {
            if (sections.get(i).contains(point)) {
                return i;
            }
        }
        return null;
    }

    public MovementSections getSection(final Movement movement) {
        MovementSections movementSections = movementSectionsMap.get(movement);
        if (movementSections == null) {
            final Integer originSection = getSection(movement.getOrigin());
            final Integer destinationSection = getSection(movement.getDestination());
            movementSections = new MovementSections(originSection, destinationSection);
            movementSectionsMap.put(movement, movementSections);
        }
        return movementSections;
    }

    private void floodFill(final WorldPoint point) {
        if (getSection(point) != null) {
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

    public void toFile() throws IOException {
        toFile(DEFAULT_SECTION_MAP_ZIP_PATH);
    }

    public void toFile(final Path path) {
        try (final ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(path))) {
            outputStream.putNextEntry(SECTIONS_ZIP_ENTRY);
            saveSectionsToFile(outputStream);
            outputStream.closeEntry();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void saveSectionsToFile(final OutputStream outputStream) {
        final Gson gson = new Gson();
        final Type sectionType = new TypeToken<HashSet<WorldPoint>>() {
        }.getType();

        try {
            final JsonWriter writer = new JsonWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            writer.setIndent("  ");
            writer.beginArray();
            for (final Set<WorldPoint> section : sections) {
                gson.toJson(section, sectionType, writer);
            }
            writer.endArray();
            writer.flush();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static SectionMapper fromFile(final WorldMapProvider worldMapProvider) {
        return SectionMapper.fromFile(DEFAULT_SECTION_MAP_ZIP_PATH, worldMapProvider);
    }

    public static SectionMapper fromFile(final Path filepath, final WorldMapProvider worldMapProvider) {
        List<Set<WorldPoint>> sections = null;
        try (final ZipInputStream inputStream = new ZipInputStream(Files.newInputStream(filepath))) {
            ZipEntry zipEntry;
            while ((zipEntry = inputStream.getNextEntry()) != null) {
                if (SECTIONS_ZIP_ENTRY.getName().equals(zipEntry.getName())) {
                    sections = getSectionsFromFile(inputStream);
                }
            }

        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        if (sections == null) {
            throw new RuntimeException(String.format("%s not found/parsed correctly from %s.",
                    SECTIONS_ZIP_ENTRY.getName(), filepath.getFileName()));
        }

        final SectionMapper sectionMapper = new SectionMapper(worldMapProvider);
        sectionMapper.sections = sections;
        return sectionMapper;
    }

    private static List<Set<WorldPoint>> getSectionsFromFile(final InputStream inputStream) {
        final Gson gson = new Gson();
        final Type sectionType = new TypeToken<HashSet<WorldPoint>>() {
        }.getType();

        final List<Set<WorldPoint>> sections = new ArrayList<>();
        try {
            final JsonReader reader = new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            reader.beginArray();
            while (reader.hasNext()) {
                final Set<WorldPoint> section = gson.fromJson(reader, sectionType);
                sections.add(section);
            }
            reader.endArray();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        return sections;
    }
}
