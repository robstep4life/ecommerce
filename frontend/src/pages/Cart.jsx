import { useEffect, useState } from "react";
import { getCart } from "../lib/api";

export default function Cart() {
  const [cart, setCart] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const c = await getCart();
        setCart(c);
      } catch {
        setCart(null);
      }
    })();
  }, []);

  if (!cart) {
    return (
      <div className="card" style={{ padding: 18 }}>
        <h2 style={{ marginTop: 0 }}>Your Cart</h2>
        <p style={{ color:"#64748b" }}>Login first, then add products to cart.</p>
      </div>
    );
  }

  return (
    <div className="card" style={{ padding: 18 }}>
      <h2 style={{ marginTop: 0 }}>Your Cart</h2>
      <div style={{ display:"grid", gap: 10 }}>
        {(cart.items || []).map(it => (
          <div key={it.itemId} style={{ display:"flex", justifyContent:"space-between", gap: 10 }}>
            <div>
              <b>{it.name}</b> <span style={{ color:"#64748b" }}>x {it.quantity}</span>
            </div>
            <div><b>${it.lineTotal}</b></div>
          </div>
        ))}
      </div>
      <hr style={{ border:"0", borderTop:"1px solid rgba(15,23,42,.08)", margin:"14px 0" }} />
      <div style={{ display:"flex", justifyContent:"space-between" }}>
        <b>Total</b>
        <b>${cart.total}</b>
      </div>
    </div>
  );
}
