package unittests.utils;

import org.junit.Assert;
import org.junit.Test;
import shortestpath.utils.Util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class UtilTest {
    @Test
    public void testPathToResourcePath() {
        final Path path = Paths.get("src/main/resources/collision-map.zip");
        final String result = Util.pathToResourcePath(path);
        Assert.assertEquals("/collision-map.zip", result);
    }

    @Test
    public void testPathToResourcePath_throwsOnWrongInput() {
        final Path path = Paths.get("src/main/java/shortestpath/ShortestPathPlugin.java");
        Assert.assertThrows(RuntimeException.class, () -> Util.pathToResourcePath(path));
    }
}
