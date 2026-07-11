package com.ecom.etl.service;

import com.ecom.etl.model.DailyRevenueSummary;
import com.ecom.etl.repository.DailyRevenueSummaryRepository;
import com.ecom.etl.repository.FactOrderLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Periodic batch transformation: rolls the line-item-grain fact table up into a
 * daily revenue summary table. Runs every 30s in this demo; in production this
 * would be a nightly Spring Batch job or an Airflow DAG.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DailyAggregationJob {

    private final FactOrderLineRepository factOrderLineRepository;
    private final DailyRevenueSummaryRepository dailyRevenueSummaryRepository;

    @Scheduled(fixedRate = 30000, initialDelay = 15000)
    @Transactional
    public void rebuildDailySummary() {
        var rows = factOrderLineRepository.computeDailyAggregates();
        for (var row : rows) {
            DailyRevenueSummary summary = dailyRevenueSummaryRepository
                    .findById(row.getDay().toLocalDate())
                    .orElseGet(DailyRevenueSummary::new);
            summary.setDay(row.getDay().toLocalDate());
            summary.setTotalRevenue(row.getTotalRevenue());
            summary.setConfirmedOrders(row.getConfirmedOrders() == null ? 0 : row.getConfirmedOrders());
            dailyRevenueSummaryRepository.save(summary);
        }
        log.info("Daily aggregation job rebuilt {} day(s) of revenue summary", rows.size());
    }
}
