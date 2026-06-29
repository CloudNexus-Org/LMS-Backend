package com.lms.catalog.repository;

import com.lms.catalog.model.Testimonial;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {
}
