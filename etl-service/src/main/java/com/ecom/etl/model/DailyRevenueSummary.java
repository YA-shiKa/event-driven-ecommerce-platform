package com.ecom.etl.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Materialized aggregate table, rebuilt periodically by the ETL scheduled job.
 * This is the "transformation" step: raw event-level facts -> a rollup ready
 * for dashboards, without scanning the full fact table on every request.
 */
@Entity
@Table(name = "daily_revenue_summary")
@Getter
@Setter
public class DailyRevenueSummary {

    @Id
    private LocalDate day;

    @Column(nullable = false)
    private BigDecimal totalRevenue;

    @Column(nullable = false)
    private Long confirmedOrders;
}
