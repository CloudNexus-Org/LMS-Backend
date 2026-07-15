package com.lms.certificate.seed;

import com.lms.certificate.model.Certificate;
import com.lms.certificate.model.CertificateTemplate;
import com.lms.certificate.repository.CertificateRepository;
import com.lms.certificate.repository.CertificateTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CertificateRepository certificateRepository;
    private final CertificateTemplateRepository templateRepository;

    private static final Long SEED_USER_ID = 3L;

    @Override
    @Transactional
    public void run(String... args) {
        if (certificateRepository.count() > 0) {
            return;
        }

        templateRepository.saveAll(List.of(
                CertificateTemplate.builder()
                        .trackId("cloud")
                        .templateHtml("<html><body><h1>Cloud Engineer Certificate</h1></body></html>")
                        .logoUrl("https://cloudnexus.com/logo.png")
                        .build(),
                CertificateTemplate.builder()
                        .trackId("ai")
                        .templateHtml("<html><body><h1>AI Engineer Certificate</h1></body></html>")
                        .logoUrl("https://cloudnexus.com/logo.png")
                        .build()
        ));

        certificateRepository.saveAll(List.of(
                Certificate.builder()
                        .code("CN-AWSA-8412")
                        .userId(SEED_USER_ID)
                        .trackId("cloud")
                        .title("AWS Solution Architect")
                        .description("An immersive, project-backed track in cloud architecture and DevOps")
                        .recipientName("Alex Chen")
                        .issueDate(LocalDate.of(2026, 3, 1))
                        .duration("24h")
                        .mentorName("Dr. Arjan Singh")
                        .track("Cloud Architecture")
                        .status("verified")
                        .verifyUrl("cloudnexus.com/verify/CN-AWSA-8412")
                        .pdfUrl("/api/certificates/download/CN-AWSA-8412.pdf")
                        .build(),
                Certificate.builder()
                        .code("CN-AZAI-9921")
                        .userId(SEED_USER_ID)
                        .trackId("ai")
                        .title("Azure Generative AI Services")
                        .description("Advanced patterns for building scalable AI systems on Azure")
                        .recipientName("Alex Chen")
                        .issueDate(LocalDate.of(2026, 1, 15))
                        .duration("18h")
                        .mentorName("Sarah Jenkins")
                        .track("AI / ML Engineering")
                        .status("verified")
                        .verifyUrl("cloudnexus.com/verify/CN-AZAI-9921")
                        .pdfUrl("/api/certificates/download/CN-AZAI-9921.pdf")
                        .build()
        ));
    }
}
