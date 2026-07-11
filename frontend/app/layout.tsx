import "./globals.css";
import type { Metadata } from "next";
import Link from "next/link";

export const metadata: Metadata = {
  title: "Order Ops Console",
  description: "Live view of the event-driven order pipeline",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        <div className="shell">
          <header className="topbar">
            <div className="brand">
              <span className="brand-mark" aria-hidden="true" />
              <span className="brand-name">order-ops</span>
            </div>
            <nav className="nav">
              <Link href="/">console</Link>
              <Link href="/orders/new">place order</Link>
            </nav>
          </header>
          <main>{children}</main>
        </div>
      </body>
    </html>
  );
}
