export const ORDER_API = process.env.NEXT_PUBLIC_ORDER_API ?? "http://localhost:8081";
export const INVENTORY_API = process.env.NEXT_PUBLIC_INVENTORY_API ?? "http://localhost:8082";
export const PAYMENT_API = process.env.NEXT_PUBLIC_PAYMENT_API ?? "http://localhost:8083";

export type OrderStatus =
  | "PENDING"
  | "INVENTORY_RESERVED"
  | "INVENTORY_REJECTED"
  | "PAID"
  | "PAYMENT_FAILED"
  | "CONFIRMED"
  | "CANCELLED";

export interface OrderItem {
  productId: string;
  quantity: number;
  unitPrice: number;
}

export interface Order {
  id: string;
  customerEmail: string;
  status: OrderStatus;
  totalAmount: number;
  items: OrderItem[];
  createdAt: string;
  updatedAt: string;
}

export interface Product {
  id: string;
  name: string;
  availableQuantity: number;
}

export async function fetchOrders(): Promise<Order[]> {
  const res = await fetch(`${ORDER_API}/api/orders`, { cache: "no-store" });
  if (!res.ok) throw new Error(`Failed to load orders (${res.status})`);
  return res.json();
}

export async function fetchProducts(): Promise<Product[]> {
  const res = await fetch(`${INVENTORY_API}/api/products`, { cache: "no-store" });
  if (!res.ok) throw new Error(`Failed to load products (${res.status})`);
  return res.json();
}

export async function createOrder(payload: {
  customerEmail: string;
  items: { productId: string; quantity: number }[];
}): Promise<Order> {
  const res = await fetch(`${ORDER_API}/api/orders`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!res.ok) throw new Error(`Failed to place order (${res.status})`);
  return res.json();
}

export function subscribeToOrderUpdates(
  onUpdate: (order: Order) => void,
  onConnect?: () => void
): () => void {
  const source = new EventSource(`${ORDER_API}/api/orders/stream`);
  source.onopen = () => onConnect?.();
  source.addEventListener("order-update", (event) => {
    const data = JSON.parse((event as MessageEvent).data);
    onUpdate(data);
  });
  return () => source.close();
}

export const STATUS_LABEL: Record<OrderStatus, string> = {
  PENDING: "pending",
  INVENTORY_RESERVED: "stock reserved",
  INVENTORY_REJECTED: "out of stock",
  PAID: "paid",
  PAYMENT_FAILED: "payment failed",
  CONFIRMED: "confirmed",
  CANCELLED: "cancelled",
};

export function statusBadgeClass(status: OrderStatus): string {
  switch (status) {
    case "CONFIRMED":
    case "PAID":
      return "badge badge-confirmed";
    case "INVENTORY_REJECTED":
    case "PAYMENT_FAILED":
    case "CANCELLED":
      return "badge badge-failed";
    case "PENDING":
      return "badge badge-pending";
    default:
      return "badge badge-progress";
  }
}