package com.lms.catalog.repository;

import com.lms.catalog.model.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Long> {

    List<Faq> findAllByOrderByOrderIndexAsc();
}
