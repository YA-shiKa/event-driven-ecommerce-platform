"use client";

import { useEffect, useState } from "react";
import {
  fetchOrders,
  fetchProducts,
  subscribeToOrderUpdates,
  statusBadgeClass,
  STATUS_LABEL,
  type Order,
  type Product,
} from "@/lib/api";

function timeAgo(iso: string): string {
  const seconds = Math.floor((Date.now() - new Date(iso).getTime()) / 1000);
  if (seconds < 60) return `${seconds}s ago`;
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m ago`;
  return `${Math.floor(seconds / 3600)}h ago`;
}

function stockClass(qty: number): string {
  if (qty === 0) return "stock-bar-fill empty";
  if (qty < 10) return "stock-bar-fill low";
  return "stock-bar-fill";
}

export default function DashboardPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchOrders().then(setOrders).catch((e) => setError(e.message));
    fetchProducts().then(setProducts).catch((e) => setError(e.message));

    const unsubscribe = subscribeToOrderUpdates(
      (updated) => {
        setOrders((prev) => {
          const exists = prev.some((o) => o.id === updated.id);
          return exists
            ? prev.map((o) => (o.id === updated.id ? updated : o))
            : [updated, ...prev];
        });
      },
      () => setConnected(true)
    );

    return unsubscribe;
  }, []);

  return (
    <div>
      <h1>Order fulfillment console</h1>
      <p className="subtitle">
        {connected ? "● live — streaming order events over SSE" : "○ connecting to event stream…"}
      </p>

      {error && <div className="status-line err" style={{ marginBottom: 20 }}>{error}</div>}

      <div className="grid">
        <section className="panel">
          <p className="panel-title">Recent orders</p>
          {orders.length === 0 && <div className="empty">No orders yet — place one to see the saga run live.</div>}
          {orders.map((order) => (
            <div className="order-row" key={order.id}>
              <div>
                <div className="order-id">{order.id.slice(0, 8)}</div>
                <div className="order-meta">{order.customerEmail} · ${order.totalAmount.toFixed(2)} · {timeAgo(order.createdAt)}</div>
              </div>
              <span className={statusBadgeClass(order.status)}>{STATUS_LABEL[order.status]}</span>
            </div>
          ))}
        </section>

        <section className="panel">
          <p className="panel-title">Inventory</p>
          {products.map((p) => (
            <div className="product-row" key={p.id}>
              <div>
                <div>{p.name}</div>
                <div className="stock-bar">
                  <div className={stockClass(p.availableQuantity)} style={{ width: `${Math.min(100, p.availableQuantity * 2)}%` }} />
                </div>
              </div>
              <span className="order-meta">{p.availableQuantity} in stock</span>
            </div>
          ))}
        </section>
      </div>
    </div>
  );
}