package com.ecom.etl.repository;

import com.ecom.etl.model.DailyRevenueSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface DailyRevenueSummaryRepository extends JpaRepository<DailyRevenueSummary, LocalDate> {
}
