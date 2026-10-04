package com.samwallflower.safewalk.integration.googlemaps;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoogleMapsClientReplayTest {

    @Test
    void evalOn_replaysCachedResponse_withoutCallingGoogle(@TempDir Path dir) throws Exception {
        // geoApiContext is null: any real Google call would fail
        GoogleMapsClient client = new GoogleMapsClient(null);
        ReflectionTestUtils.setField(client, "evalEnabled", true);
        ReflectionTestUtils.setField(client, "cacheDir", dir.toString());

        Files.writeString(dir.resolve("walking_52.950000_-1.150000_52.960000_-1.140000.json"),
                "[{\"polyline\":\"abc\",\"distanceMeters\":1234.5},{\"polyline\":\"def\",\"distanceMeters\":1500.0}]");

        List<GoogleRouteCandidate> result = client.getAlternativeRoutes(52.95, -1.15, 52.96, -1.14);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPolyline()).isEqualTo("abc");
        assertThat(result.get(0).getActualDistanceMeters()).isEqualTo(1234.5);
        assertThat(result.get(1).getPolyline()).isEqualTo("def");
    }

    @Test
    void evalOff_ignoresCache(@TempDir Path dir) throws Exception {
        GoogleMapsClient client = new GoogleMapsClient(null);
        ReflectionTestUtils.setField(client, "cacheDir", dir.toString());
        Files.writeString(dir.resolve("walking_52.950000_-1.150000_52.960000_-1.140000.json"),
                "[{\"polyline\":\"abc\",\"distanceMeters\":1.0}]");

        // eval off => goes to Google (null context => fails), cache is not read
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> client.getAlternativeRoutes(52.95, -1.15, 52.96, -1.14))
                .isInstanceOf(RuntimeException.class);
    }
}
