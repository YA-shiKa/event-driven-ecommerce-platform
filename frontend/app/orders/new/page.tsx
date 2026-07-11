"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { createOrder, fetchProducts, type Product } from "@/lib/api";

interface Line {
  productId: string;
  quantity: number;
}

export default function NewOrderPage() {
  const router = useRouter();
  const [products, setProducts] = useState<Product[]>([]);
  const [email, setEmail] = useState("customer@example.com");
  const [lines, setLines] = useState<Line[]>([{ productId: "", quantity: 1 }]);
  const [status, setStatus] = useState<{ type: "ok" | "err"; message: string } | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchProducts().then((p) => {
      setProducts(p);
      if (p.length > 0) setLines([{ productId: p[0].id, quantity: 1 }]);
    });
  }, []);

  function updateLine(index: number, patch: Partial<Line>) {
    setLines((prev) => prev.map((l, i) => (i === index ? { ...l, ...patch } : l)));
  }

  function addLine() {
    setLines((prev) => [...prev, { productId: products[0]?.id ?? "", quantity: 1 }]);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setStatus(null);
    try {
      const order = await createOrder({ customerEmail: email, items: lines });
      setStatus({ type: "ok", message: `Order ${order.id.slice(0, 8)} placed — watch it move through the pipeline on the console.` });
      setTimeout(() => router.push("/"), 1200);
    } catch (err) {
      setStatus({ type: "err", message: (err as Error).message });
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <h1>Place an order</h1>
      <p className="subtitle">
        This submits to order-service, which publishes order.created to Kafka — inventory, payment, and analytics all react from here.
      </p>

      <form onSubmit={handleSubmit}>
        <label>
          Customer email
          <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" required />
        </label>

        {lines.map((line, i) => (
          <div className="line-item" key={i}>
            <label>
              Product
              <select
                value={line.productId}
                onChange={(e) => updateLine(i, { productId: e.target.value })}
                required
              >
                {products.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name} ({p.availableQuantity} in stock)
                  </option>
                ))}
              </select>
            </label>
            <label>
              Qty
              <input
                type="number"
                min={1}
                value={line.quantity}
                onChange={(e) => updateLine(i, { quantity: Number(e.target.value) })}
                required
              />
            </label>
          </div>
        ))}

        <button type="button" className="secondary" onClick={addLine}>
          + add another item
        </button>

        {status && <div className={`status-line ${status.type === "ok" ? "ok" : "err"}`}>{status.message}</div>}

        <button type="submit" disabled={submitting}>
          {submitting ? "Placing order…" : "Place order"}
        </button>
      </form>
    </div>
  );
}
