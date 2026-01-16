import { useEffect, useState } from "react";
import { addToCart, getProducts } from "../lib/api";

export default function Products() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  async function load() {
    setLoading(true);
    try {
      const res = await getProducts();
      setItems(res.items || []);
    } catch {
      alert("Failed to load products");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  async function handleAdd(id) {
    try {
      await addToCart(id, 1);
    } catch (e) {
      alert("You must login to add to cart");
    }
  }

  return (
    <div>
      <div style={{ display:"flex", alignItems:"flex-end", justifyContent:"space-between", gap:16, marginBottom: 18 }}>
        <div>
          <div style={{ fontSize: 12, fontWeight: 800, color:"#7c3aed", letterSpacing: ".08em" }}>
            NEW ARRIVALS
          </div>
          <h1 style={{ margin: "6px 0 0", fontSize: 44, letterSpacing: "-0.02em" }}>Shop</h1>
          <p style={{ margin: "8px 0 0", color:"#64748b", maxWidth: 520 }}>
            Clean UI, fast checkout, and a cart that updates instantly.
          </p>
        </div>

        <button className="btn btnGhost" onClick={load} style={{ padding: "10px 14px" }}>
          Reload
        </button>
      </div>

      {loading ? (
        <p>Loading...</p>
      ) : (
        <div style={{
          display:"grid",
          gridTemplateColumns:"repeat(auto-fill, minmax(250px, 1fr))",
          gap: 18
        }}>
          {items.map(p => (
            <div key={p.id} className="card" style={{ padding: 14 }}>
              <div style={{
                height: 160,
                borderRadius: 14,
                background:
                  "linear-gradient(135deg, rgba(124,58,237,.16), rgba(255,77,157,.16))",
                display:"flex",
                alignItems:"center",
                justifyContent:"center",
                fontSize: 42
              }}>
                🧢
              </div>

              <div style={{ display:"flex", alignItems:"flex-start", justifyContent:"space-between", gap: 10, marginTop: 12 }}>
                <div>
                  <div style={{ fontWeight: 900, fontSize: 18 }}>{p.name}</div>
                  <div style={{ fontSize: 12, color:"#64748b", fontWeight: 700 }}>SKU: {p.sku}</div>
                </div>
                <div style={{ fontWeight: 900, fontSize: 18 }}>${p.price}</div>
              </div>

              <div style={{ color:"#475569", marginTop: 8 }}>{p.description}</div>
              <div style={{ marginTop: 10, fontSize: 13, color:"#64748b" }}>
                Stock: <b style={{ color:"#0f172a" }}>{p.inventory}</b>
              </div>

              <button
                className="btn btnPrimary"
                onClick={() => handleAdd(p.id)}
                style={{ width:"100%", marginTop: 12, padding: "12px 14px" }}
              >
                Add to cart
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
