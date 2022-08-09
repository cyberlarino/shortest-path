package shortestpath.worldmap.sections;

import lombok.Getter;
import lombok.Value;

@Value
public class MovementSections {
    @Getter
    Integer originSection;
    @Getter
    Integer destinationSection;
}
