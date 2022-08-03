package shortestpath.worldmap.sections;

import lombok.Getter;

public class MovementSections {
    @Getter
    private final Integer originSection;
    @Getter
    private final Integer destinationSection;

    public MovementSections(final Integer originSection, final Integer destinationSection) {
        this.originSection = originSection;
        this.destinationSection = destinationSection;
    }
}
