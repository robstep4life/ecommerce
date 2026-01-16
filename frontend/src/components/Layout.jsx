import { Link, NavLink, Outlet } from "react-router-dom";
import { useEffect, useState } from "react";
import { getCart } from "../lib/api";

function getToken() {
  return localStorage.getItem("token");
}

export default function Layout() {
  const [cartCount, setCartCount] = useState(0);

  async function refreshCartCount() {
    try {
      const token = getToken();
      if (!token) { setCartCount(0); return; }
      const cart = await getCart();
      const count = (cart?.items || []).reduce((sum, it) => sum + (it.quantity || 0), 0);
      setCartCount(count);
    } catch {
      setCartCount(0);
    }
  }

  useEffect(() => {
    refreshCartCount();
    window.addEventListener("cart:changed", refreshCartCount);
    return () => window.removeEventListener("cart:changed", refreshCartCount);
  }, []);

  const loggedIn = !!getToken();

  return (
    <div>
      <header style={{ borderBottom: "1px solid rgba(15,23,42,.06)", background: "rgba(255,255,255,.6)", backdropFilter: "blur(10px)" }}>
        <div className="container" style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 16 }}>
          <Link to="/" style={{ display: "flex", alignItems: "center", gap: 10, fontWeight: 900, fontSize: 20 }}>
            <span style={{ fontSize: 22 }}>🛍️</span>
            Ecommerce
          </Link>

          <nav style={{ display: "flex", alignItems: "center", gap: 16, flexWrap: "wrap" }}>
            <NavLink to="/" style={({isActive}) => ({
              padding: "8px 10px", borderRadius: 12,
              background: isActive ? "rgba(124,58,237,.10)" : "transparent",
              fontWeight: 700
            })}>
              Products
            </NavLink>

            <NavLink to="/cart" style={({isActive}) => ({
              padding: "8px 10px", borderRadius: 12,
              background: isActive ? "rgba(124,58,237,.10)" : "transparent",
              fontWeight: 700,
              display:"flex", alignItems:"center", gap:8
            })}>
              Cart <span className="badge">{cartCount}</span>
            </NavLink>

            <NavLink to="/checkout" style={({isActive}) => ({
              padding: "8px 10px", borderRadius: 12,
              background: isActive ? "rgba(124,58,237,.10)" : "transparent",
              fontWeight: 700
            })}>
              Checkout
            </NavLink>

            <NavLink to="/login" style={({isActive}) => ({
              padding: "8px 10px", borderRadius: 12,
              background: isActive ? "rgba(124,58,237,.10)" : "transparent",
              fontWeight: 700
            })}>
              {loggedIn ? "Account" : "Login"}
            </NavLink>

            <span style={{
              padding:"8px 12px",
              borderRadius: 999,
              background: loggedIn ? "rgba(16,185,129,.14)" : "rgba(15,23,42,.06)",
              fontWeight: 800,
              color: loggedIn ? "#047857" : "#334155"
            }}>
              {loggedIn ? "Logged in" : "Guest"}
            </span>
          </nav>
        </div>
      </header>

      <main className="container" style={{ paddingTop: 26 }}>
        <Outlet />
      </main>
    </div>
  );
}
