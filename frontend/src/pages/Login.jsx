import { useState } from "react";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  async function login() {
    const r = await fetch("http://localhost:8080/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password })
    });

    const data = await r.json();
    localStorage.setItem("token", data.token);
    alert("Logged in!");
  }

  return (
    <div style={{ padding: 24, maxWidth: 400 }}>
      <h1>Login</h1>

      <input
        placeholder="Email"
        value={email}
        onChange={e => setEmail(e.target.value)}
        style={{ width: "100%", padding: 10, marginBottom: 10 }}
      />

      <input
        type="password"
        placeholder="Password"
        value={password}
        onChange={e => setPassword(e.target.value)}
        style={{ width: "100%", padding: 10 }}
      />

      <button
        onClick={login}
        style={{
          marginTop: 12,
          width: "100%",
          padding: 12,
          background: "#000",
          color: "#fff",
          border: "none",
          borderRadius: 12
        }}
      >
        Sign in
      </button>
    </div>
  );
}
