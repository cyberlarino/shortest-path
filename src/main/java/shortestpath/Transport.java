package shortestpath;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * This class represents a travel point between two WorldPoints.
 */
public class Transport {
    /** The starting point of this transport */
    @Getter
    private final WorldPoint origin;

    /** The ending point of this transport */
    @Getter
    private final WorldPoint destination;

    /** The agility level required to use this transport */
    @Getter
    private final int agilityLevelRequired;

    /** The ranged level required to use this transport */
    @Getter
    private final int rangedLevelRequired;

    /** The strength level required to use this transport */
    @Getter
    private final int strengthLevelRequired;

    Transport(final String line) {
        final String DELIM = " ";

        String[] parts = line.split("\t");

        String[] parts_origin = parts[0].split(DELIM);
        String[] parts_destination = parts[1].split(DELIM);

        origin = new WorldPoint(
            Integer.parseInt(parts_origin[0]),
            Integer.parseInt(parts_origin[1]),
            Integer.parseInt(parts_origin[2]));
        destination = new WorldPoint(
            Integer.parseInt(parts_destination[0]),
            Integer.parseInt(parts_destination[1]),
            Integer.parseInt(parts_destination[2]));

        int agilityLevel = 0;
        int rangedLevel = 0;
        int strengthLevel = 0;

        if (parts.length >= 4 && !parts[3].startsWith("\"")) {
            String[] requirements = parts[3].split(";");

            if (requirements.length >= 1) {
                agilityLevel = Integer.parseInt(requirements[0].split(DELIM)[0]);
            }
            if (requirements.length >= 2) {
                rangedLevel = Integer.parseInt(requirements[1].split(DELIM)[0]);
            }
            if (requirements.length >= 3) {
                strengthLevel = Integer.parseInt(requirements[2].split(DELIM)[0]);
            }
        }

        agilityLevelRequired = agilityLevel;
        rangedLevelRequired = rangedLevel;
        strengthLevelRequired = strengthLevel;
    }

    Transport(final WorldPoint origin, final WorldPoint destination, int agilityLevelRequired, int rangedLevelRequired, int strengthLevelRequired) {
        this.origin = origin;
        this.destination = destination;
        this.agilityLevelRequired = agilityLevelRequired;
        this.rangedLevelRequired = rangedLevelRequired;
        this.strengthLevelRequired = strengthLevelRequired;
    }

    Transport(final WorldPoint origin, final WorldPoint destination) {
        this(origin, destination, 0, 0, 0);
    }

    public static Map<WorldPoint, List<Transport>> fromFile(final String filepath) {
        HashMap<WorldPoint, List<Transport>> transports = new HashMap<>();
        try {
            Path path = Paths.get(filepath);
            String s = Files.readString(path);
            Scanner scanner = new Scanner(s);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                Transport transport = new Transport(line);
                WorldPoint origin = transport.getOrigin();
                transports.computeIfAbsent(origin, k -> new ArrayList<>()).add(transport);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return transports;
    }
}
