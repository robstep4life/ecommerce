import { Link, NavLink } from "react-router-dom";

export default function Navbar({ isAuthed, onLogout, cartCount = 0 }) {
  return (
    <header className="navbar">
      <div className="container navbar-inner">
        <Link to="/products" className="brand">
          <span className="brand-dot" />
          Robben<span className="brand-accent">Store</span>
        </Link>

        <nav className="navbar-links">
          <NavLink to="/products" className={({ isActive }) => (isActive ? "navlink active" : "navlink")}>
            Products
          </NavLink>

          <NavLink to="/cart" className={({ isActive }) => (isActive ? "navlink active" : "navlink")}>
            <span className="nav-cart">
              Cart
              <span className="cart-badge">{cartCount}</span>
            </span>
          </NavLink>

          <NavLink to="/checkout" className={({ isActive }) => (isActive ? "navlink active" : "navlink")}>
            Checkout
          </NavLink>
          <NavLink to="/pay" className={({ isActive }) => (isActive ? "navlink active" : "navlink")}>
            Pay
          </NavLink>

          {!isAuthed ? (
            <>
              <NavLink to="/login" className={({ isActive }) => (isActive ? "navlink active" : "navlink")}>
                Login
              </NavLink>
              <span className="pill">Guest</span>
            </>
          ) : (
            <>
              <span className="pill pill-ok">Logged in</span>
              <button className="btn btn-ghost" onClick={onLogout} type="button">
                Logout
              </button>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
