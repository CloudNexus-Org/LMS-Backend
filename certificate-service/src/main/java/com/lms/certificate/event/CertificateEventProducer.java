package com.lms.certificate.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CertificateEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCertificateIssued(Long userId, String trackId, String code) {
        kafkaTemplate.send("certificate.issued", code, Map.of(
                "userId", userId,
                "trackId", trackId,
                "code", code
        ));
    }
}
