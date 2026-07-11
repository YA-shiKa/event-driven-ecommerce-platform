package com.ecom.etl.controller;

import com.ecom.etl.model.DailyRevenueSummary;
import com.ecom.etl.repository.DailyRevenueSummaryRepository;
import com.ecom.etl.repository.FactOrderLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read side of the ETL pipeline - what a BI tool or the frontend dashboard would query. */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final FactOrderLineRepository factOrderLineRepository;
    private final DailyRevenueSummaryRepository dailyRevenueSummaryRepository;

    @GetMapping("/revenue-by-product")
    public List<FactOrderLineRepository.ProductRevenueRow> revenueByProduct() {
        return factOrderLineRepository.revenueByProduct();
    }

    @GetMapping("/daily-revenue")
    public List<DailyRevenueSummary> dailyRevenue() {
        return dailyRevenueSummaryRepository.findAll();
    }
}
