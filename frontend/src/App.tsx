import { useEffect, useMemo, useState } from "react";
import "./index.css";

type Product = {
  id: number;
  sku: string;
  name: string;
  description?: string;
  price: number;
  inventory: number;
  active: boolean;
};

const API = import.meta.env.VITE_API_URL || "http://localhost:8080";

function money(n: number) {
  return new Intl.NumberFormat(undefined, { style: "currency", currency: "USD" }).format(n);
}

function emojiFor(name: string) {
  const s = name.toLowerCase();
  if (s.includes("hoodie")) return "🧥";
  if (s.includes("shirt") || s.includes("tee")) return "👕";
  if (s.includes("shoe") || s.includes("sneaker")) return "👟";
  if (s.includes("cap") || s.includes("hat")) return "🧢";
  if (s.includes("bag")) return "👜";
  if (s.includes("watch")) return "⌚️";
  return "🛍️";
}

function getToken() {
  return localStorage.getItem("token") || "";
}

async function apiFetch(path: string, init?: RequestInit) {
  const token = getToken();
  const headers: Record<string, string> = {
    Accept: "application/json",
    ...(init?.headers as any),
  };
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(`${API}${path}`, { ...init, headers });
  return res;
}

export default function App() {
  const [route, setRoute] = useState<string>(() => window.location.hash.replace("#", "") || "products");

  useEffect(() => {
    const onHash = () => setRoute(window.location.hash.replace("#", "") || "products");
    window.addEventListener("hashchange", onHash);
    return () => window.removeEventListener("hashchange", onHash);
  }, []);

  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState<string>("");

  const [q, setQ] = useState("");
  const [tab, setTab] = useState<"Discover" | "Popular" | "New" | "Sale" | "Accessories">("Discover");

  const userLabel = useMemo(() => {
    const token = getToken();
    if (!token) return "Guest";
    return "Logged in";
  }, [route]);

  async function loadProducts() {
    setLoading(true);
    setErr("");
    try {
      const res = await apiFetch("/products");
      if (!res.ok) {
        const t = await res.text().catch(() => "");
        throw new Error(`Failed to load products (${res.status}) ${t}`);
      }
      const data = await res.json();

      // backend returns either array or paged object with items
      const list: Product[] = Array.isArray(data) ? data : (data.items || []);
      setProducts(list.filter(p => p.active !== false));
    } catch (e: any) {
      setErr(e?.message || "Failed to load products");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    // only fetch on products page
    if (route === "products") loadProducts();
  }, [route]);

  const filtered = useMemo(() => {
    let list = [...products];

    // tabs are aesthetic for now, but we can simulate:
    if (tab === "Popular") list = list.slice().sort((a, b) => b.inventory - a.inventory);
    if (tab === "New") list = list.slice().sort((a, b) => b.id - a.id);
    if (tab === "Sale") list = list.slice().sort((a, b) => a.price - b.price);

    const s = q.trim().toLowerCase();
    if (s) {
      list = list.filter(p =>
        p.name.toLowerCase().includes(s) ||
        (p.description || "").toLowerCase().includes(s) ||
        p.sku.toLowerCase().includes(s)
      );
    }
    return list;
  }, [products, q, tab]);

  // For now, this just shows a toast. Later we can POST to /cart/items.
  const [toast, setToast] = useState<string>("");
  function addToCart(p: Product) {
    setToast(`Added “${p.name}” to cart (wire API next).`);
    window.setTimeout(() => setToast(""), 2200);
  }

  function NavLink({ to, label }: { to: string; label: string }) {
    const active = route === to;
    return (
      <a className={active ? "active" : ""} href={`#${to}`}>
        {label}
      </a>
    );
  }

  return (
    <>
      <div className="nav">
        <div className="container">
          <div className="nav-inner">
            <div className="brand">
              <div className="brand-badge">🛍️</div>
              Ecommerce
            </div>

            <div className="search" title="Search products">
              <span style={{ color: "var(--muted)", fontWeight: 900 }}>🔎</span>
              <input
                value={q}
                onChange={(e) => setQ(e.target.value)}
                placeholder="Search products, SKU, description…"
              />
            </div>

            <div className="nav-links">
              <NavLink to="products" label="Products" />
              <NavLink to="cart" label="Cart" />
              <NavLink to="checkout" label="Checkout" />
              <NavLink to="pay" label="Pay" />
              <NavLink to="login" label="Login" />
            </div>

            <span className="pill ghost">{userLabel}</span>
          </div>
        </div>
      </div>

      {route === "products" && (
        <>
          <div className="container hero">
            <h1>Ecommerce</h1>
            <p>Discover clean product cards, smooth layout, and a storefront that actually feels like a shop.</p>

            <div className="hero-row">
              <div className="tabs" aria-label="Category tabs">
                {(["Discover", "Popular", "New", "Sale", "Accessories"] as const).map((t) => (
                  <div
                    key={t}
                    className={`tab ${tab === t ? "active" : ""}`}
                    onClick={() => setTab(t)}
                    role="button"
                    tabIndex={0}
                  >
                    {t}
                  </div>
                ))}
              </div>

              <div className="actions">
                <button className="btn" onClick={() => { setQ(""); setTab("Discover"); }}>
                  Reset
                </button>
                <button className="btn primary" onClick={loadProducts} disabled={loading}>
                  {loading ? "Loading…" : "Reload"}
                </button>
              </div>
            </div>

            {toast && <div className="toast">{toast}</div>}
            {err && <div className="toast" style={{ color: "#ef4444", fontWeight: 900 }}>{err}</div>}
          </div>

          <div className="container section">
            <div className="grid">
              {filtered.map((p) => {
                const out = !p.active || p.inventory <= 0;
                return (
                  <div className="card" key={p.id}>
                    <div className="thumb">
                      <div className="emoji">{emojiFor(p.name)}</div>
                    </div>

                    <div className="card-body">
                      <div className="row">
                        <div>
                          <h3 className="title">{p.name}</h3>
                          <div className="meta">SKU: {p.sku}</div>
                        </div>
                        <div className="price">{money(p.price)}</div>
                      </div>

                      <p className="desc">{p.description || "—"}</p>

                      <div className="row" style={{ marginTop: 10 }}>
                        <div className="stock">
                          {out ? (
                            <span className="badge out">Out of stock</span>
                          ) : (
                            <span className="badge">In stock: {p.inventory}</span>
                          )}
                        </div>
                      </div>

                      <div className="card-actions">
                        <button className="btn primary full" disabled={out} onClick={() => addToCart(p)}>
                          Add to Cart
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}

              {!loading && filtered.length === 0 && (
                <div className="toast" style={{ gridColumn: "1 / -1" }}>
                  No products matched your search.
                </div>
              )}
            </div>

            <div className="footer-space" />
          </div>
        </>
      )}

      {route !== "products" && (
        <div className="container section">
          <div className="card" style={{ padding: 18 }}>
            <h2 style={{ margin: 0, letterSpacing: "-0.02em" }}>
              {route.charAt(0).toUpperCase() + route.slice(1)}
            </h2>
            <p style={{ marginTop: 10, color: "var(--muted)", fontWeight: 700 }}>
              This page is intentionally simple for now — your backend is working, so next we’ll make these pages
              functional (Cart → Checkout → Stripe Session → Webhook update).
            </p>
            <div style={{ display: "flex", gap: 10, marginTop: 14, flexWrap: "wrap" }}>
              <a className="btn" href="#products">Go to Products</a>
              <a className="btn primary" href="#login">Go to Login</a>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
