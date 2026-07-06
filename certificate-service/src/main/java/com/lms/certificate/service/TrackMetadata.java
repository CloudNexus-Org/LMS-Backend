package com.lms.certificate.service;

import java.util.Map;
import java.util.Optional;

public final class TrackMetadata {

    public record Meta(String title, String description, String duration, String track, String mentor, String codePrefix) {}

    private static final Map<String, Meta> TRACKS = Map.ofEntries(
            Map.entry("cloud", new Meta(
                    "AWS Solution Architect",
                    "An immersive, project-backed track in cloud architecture and DevOps",
                    "24h", "Cloud Architecture", "Dr. Arjan Singh", "CN-AWSA")),
            Map.entry("ai", new Meta(
                    "Azure Generative AI Services",
                    "Advanced patterns for building scalable AI systems on Azure",
                    "18h", "AI / ML Engineering", "Sarah Jenkins", "CN-AZAI")),
            Map.entry("fullstack", new Meta(
                    "Modern JavaScript",
                    "End-to-end full-stack builder track from idea to shipped product",
                    "30h", "Full-Stack Engineering", "Prof. David Miller", "CN-FSJS")),
            Map.entry("devops", new Meta(
                    "Docker & Containerization",
                    "Production DevOps: containers, Kubernetes, CI/CD, and SRE practices",
                    "20h", "DevOps Engineering", "James Wilson", "CN-DEVO")),
            Map.entry("data", new Meta(
                    "Python for Data Engineering",
                    "Data pipelines, warehousing, and orchestration at scale",
                    "28h", "Data Engineering", "Angela Yu", "CN-DATA")),
            Map.entry("backend", new Meta(
                    "High Performance Go (Golang)",
                    "API design and backend services built for millions of requests",
                    "26h", "Backend Architecture", "Elena Rodriguez", "CN-BACK"))
    );

    private TrackMetadata() {}

    public static Optional<Meta> forTrack(String trackId) {
        return Optional.ofNullable(TRACKS.get(trackId));
    }
}
