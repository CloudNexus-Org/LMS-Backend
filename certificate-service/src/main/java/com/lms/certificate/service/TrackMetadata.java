package com.lms.certificate.service;

import java.util.Map;
import java.util.Optional;

public final class TrackMetadata {

    public record Meta(String title, String description, String duration, String track, String mentor, String codePrefix) {}

    private static final Map<String, Meta> TRACKS = Map.of(
            "cloud", new Meta(
                    "AWS Solution Architect",
                    "An immersive, project-backed track in cloud architecture and DevOps",
                    "24h", "Cloud Architecture", "Dr. Arjan Singh", "CN-AWSA"),
            "ai", new Meta(
                    "Azure Generative AI Services",
                    "Advanced patterns for building scalable AI systems on Azure",
                    "18h", "AI / ML Engineering", "Sarah Jenkins", "CN-AZAI")
    );

    private TrackMetadata() {}

    public static Optional<Meta> forTrack(String trackId) {
        return Optional.ofNullable(TRACKS.get(trackId));
    }
}
