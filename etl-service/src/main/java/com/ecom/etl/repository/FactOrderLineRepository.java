package com.ecom.etl.repository;

import com.ecom.etl.model.FactOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FactOrderLineRepository extends JpaRepository<FactOrderLine, UUID> {

    List<FactOrderLine> findAllByOrderId(UUID orderId);

    // Revenue by product, confirmed orders only - demonstrates a hand-written
    // analytical SQL query rather than relying on ORM aggregation.
    @Query(value = """
            SELECT f.product_id AS productId,
                   SUM(f.line_total) AS revenue,
                   SUM(f.quantity) AS unitsSold
            FROM fact_order_line f
            WHERE f.order_status = 'CONFIRMED'
            GROUP BY f.product_id
            ORDER BY revenue DESC
            """, nativeQuery = true)
    List<ProductRevenueRow> revenueByProduct();

    interface ProductRevenueRow {
        String getProductId();
        java.math.BigDecimal getRevenue();
        Long getUnitsSold();
    }

    @Query(value = """
            SELECT DATE(order_created_at) AS day,
                   COALESCE(SUM(line_total) FILTER (WHERE order_status = 'CONFIRMED'), 0) AS totalRevenue,
                   COUNT(DISTINCT order_id) FILTER (WHERE order_status = 'CONFIRMED') AS confirmedOrders
            FROM fact_order_line
            GROUP BY DATE(order_created_at)
            ORDER BY day
            """, nativeQuery = true)
    List<DailyAggregateRow> computeDailyAggregates();

    interface DailyAggregateRow {
        java.sql.Date getDay();
        java.math.BigDecimal getTotalRevenue();
        Long getConfirmedOrders();
    }
}
