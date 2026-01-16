const BASE = "http://localhost:8080";

function authHeaders() {
  const token = localStorage.getItem("token");
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function getProducts() {
  const r = await fetch(`${BASE}/products`);
  if (!r.ok) throw new Error("Failed to load products");
  return r.json();
}

export async function getCart() {
  const r = await fetch(`${BASE}/cart`, {
    headers: { ...authHeaders() }
  });
  if (!r.ok) throw new Error("Failed to load cart");
  return r.json();
}

export async function addToCart(productId, quantity) {
  const r = await fetch(`${BASE}/cart/items`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...authHeaders()
    },
    body: JSON.stringify({ productId, quantity })
  });
  if (!r.ok) throw new Error("Add to cart failed");
  const data = await r.json();
  window.dispatchEvent(new Event("cart:changed"));
  return data;
}
