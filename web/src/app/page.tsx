"use client";

import { useState } from "react";
import StatusStrip from "@/components/StatusStrip";
import { login, register, ApiError } from "@/lib/api";
import { decodeToken, TokenClaims } from "@/lib/jwt";

type Mode = "login" | "register";

export default function Home() {
  const [mode, setMode] = useState<Mode>("login");
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [claims, setClaims] = useState<TokenClaims | null>(null);
  const [token, setToken] = useState<string | null>(null);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const res =
        mode === "login"
          ? await login(email, password)
          : await register(fullName, email, password);
      setToken(res.accessToken);
      setClaims(decodeToken(res.accessToken));
    } catch (err) {
      const apiErr = err as ApiError;
      setError(apiErr.message || "Something went wrong.");
    } finally {
      setLoading(false);
    }
  }

  function handleLogout() {
    setToken(null);
    setClaims(null);
    setEmail("");
    setPassword("");
    setFullName("");
  }

  return (
    <div className="shell">
      <StatusStrip />

      <main className="stage">
        {claims ? (
          <SessionPanel claims={claims} token={token!} onLogout={handleLogout} />
        ) : (
          <div className="card">
            <div className="card__eyebrow">reliability monitoring · auth console</div>
            <h1 className="card__title">
              {mode === "login" ? "Sign in" : "Create account"}
            </h1>
            <p className="card__sub">
              {mode === "login"
                ? "Authenticate against the APIFlow gateway."
                : "Register a new engineer account."}
            </p>

            <form onSubmit={handleSubmit} className="form">
              {mode === "register" && (
                <label className="field">
                  <span>Full name</span>
                  <input
                    type="text"
                    required
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    placeholder="Ada Lovelace"
                    autoComplete="name"
                  />
                </label>
              )}

              <label className="field">
                <span>Email</span>
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="ada@apiflow.dev"
                  autoComplete="email"
                />
              </label>

              <label className="field">
                <span>Password</span>
                <input
                  type="password"
                  required
                  minLength={8}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••••"
                  autoComplete={mode === "login" ? "current-password" : "new-password"}
                />
              </label>

              {error && <div className="alert">{error}</div>}

              <button type="submit" className="submit" disabled={loading}>
                {loading
                  ? "Working…"
                  : mode === "login"
                  ? "Sign in"
                  : "Create account"}
              </button>
            </form>

            <button
              type="button"
              className="toggle"
              onClick={() => {
                setMode(mode === "login" ? "register" : "login");
                setError(null);
              }}
            >
              {mode === "login"
                ? "Need an account? Register"
                : "Already have an account? Sign in"}
            </button>
          </div>
        )}
      </main>

      <style jsx>{`
        .shell {
          min-height: 100vh;
          display: flex;
          flex-direction: column;
        }
        .stage {
          flex: 1;
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 40px 20px;
        }
        .card {
          width: 100%;
          max-width: 380px;
          background: var(--panel);
          border: 1px solid var(--line);
          border-radius: 10px;
          padding: 32px 28px 28px;
        }
        .card__eyebrow {
          font-family: var(--font-mono);
          font-size: 11px;
          letter-spacing: 0.08em;
          text-transform: uppercase;
          color: var(--signal);
          margin-bottom: 14px;
        }
        .card__title {
          font-size: 24px;
          font-weight: 700;
          margin: 0 0 6px;
        }
        .card__sub {
          font-size: 13px;
          color: var(--muted);
          margin: 0 0 24px;
          line-height: 1.5;
        }
        .form {
          display: flex;
          flex-direction: column;
          gap: 16px;
        }
        .field {
          display: flex;
          flex-direction: column;
          gap: 6px;
          font-size: 12px;
          color: var(--muted);
        }
        .field input {
          background: var(--ink);
          border: 1px solid var(--line);
          border-radius: 6px;
          padding: 10px 12px;
          font-size: 14px;
          color: var(--text);
          font-family: var(--font-sans);
        }
        .field input:focus {
          border-color: var(--signal);
        }
        .field input::placeholder {
          color: var(--muted-dim);
        }
        .alert {
          background: var(--signal-down-dim);
          border: 1px solid var(--signal-down);
          color: #ffb4b6;
          font-size: 13px;
          padding: 10px 12px;
          border-radius: 6px;
          line-height: 1.4;
        }
        .submit {
          background: var(--signal);
          color: #06231a;
          border: none;
          border-radius: 6px;
          padding: 11px;
          font-weight: 700;
          font-size: 14px;
          cursor: pointer;
          margin-top: 4px;
        }
        .submit:disabled {
          opacity: 0.6;
          cursor: default;
        }
        .toggle {
          background: none;
          border: none;
          color: var(--muted);
          font-size: 12px;
          margin-top: 18px;
          cursor: pointer;
          text-decoration: underline;
          text-underline-offset: 3px;
          padding: 0;
        }
        .toggle:hover {
          color: var(--text);
        }
      `}</style>
    </div>
  );
}

function SessionPanel({
  claims,
  token,
  onLogout,
}: {
  claims: TokenClaims;
  token: string;
  onLogout: () => void;
}) {
  const rows: [string, string][] = [
    ["subject", String(claims.sub ?? claims.email ?? "—")],
    ["role", String(claims.role ?? claims.authorities?.join(", ") ?? "—")],
    [
      "issued",
      claims.iat ? new Date(claims.iat * 1000).toLocaleString() : "—",
    ],
    [
      "expires",
      claims.exp ? new Date(claims.exp * 1000).toLocaleString() : "—",
    ],
  ];

  return (
    <div className="session">
      <div className="session__eyebrow">
        <span className="dot" />
        authenticated
      </div>
      <h1 className="session__title">Session active</h1>
      <dl className="session__grid">
        {rows.map(([k, v]) => (
          <div className="session__row" key={k}>
            <dt>{k}</dt>
            <dd>{v}</dd>
          </div>
        ))}
      </dl>
      <details className="session__token">
        <summary>Access token</summary>
        <code>{token}</code>
      </details>
      <button className="session__logout" onClick={onLogout}>
        Sign out
      </button>

      <style jsx>{`
        .session {
          width: 100%;
          max-width: 420px;
          background: var(--panel);
          border: 1px solid var(--line);
          border-radius: 10px;
          padding: 32px 28px 28px;
        }
        .session__eyebrow {
          display: flex;
          align-items: center;
          gap: 8px;
          font-family: var(--font-mono);
          font-size: 11px;
          letter-spacing: 0.08em;
          text-transform: uppercase;
          color: var(--signal);
          margin-bottom: 14px;
        }
        .dot {
          width: 7px;
          height: 7px;
          border-radius: 50%;
          background: var(--signal);
          box-shadow: 0 0 0 3px var(--signal-dim);
        }
        .session__title {
          font-size: 22px;
          font-weight: 700;
          margin: 0 0 20px;
        }
        .session__grid {
          margin: 0 0 20px;
          display: flex;
          flex-direction: column;
          gap: 10px;
        }
        .session__row {
          display: flex;
          justify-content: space-between;
          gap: 12px;
          font-size: 13px;
          border-bottom: 1px solid var(--line);
          padding-bottom: 10px;
        }
        .session__row dt {
          color: var(--muted);
          font-family: var(--font-mono);
          font-size: 11px;
          text-transform: uppercase;
          letter-spacing: 0.04em;
          padding-top: 2px;
        }
        .session__row dd {
          margin: 0;
          text-align: right;
          word-break: break-word;
        }
        .session__token {
          margin-bottom: 20px;
        }
        .session__token summary {
          cursor: pointer;
          font-size: 12px;
          color: var(--muted);
        }
        .session__token code {
          display: block;
          margin-top: 10px;
          font-family: var(--font-mono);
          font-size: 11px;
          color: var(--muted);
          background: var(--ink);
          border: 1px solid var(--line);
          border-radius: 6px;
          padding: 10px;
          word-break: break-all;
          max-height: 100px;
          overflow-y: auto;
        }
        .session__logout {
          width: 100%;
          background: transparent;
          border: 1px solid var(--line);
          color: var(--text);
          border-radius: 6px;
          padding: 11px;
          font-weight: 600;
          font-size: 14px;
          cursor: pointer;
        }
        .session__logout:hover {
          border-color: var(--signal-down);
          color: var(--signal-down);
        }
      `}</style>
    </div>
  );
}
