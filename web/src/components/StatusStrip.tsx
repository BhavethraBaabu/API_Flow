"use client";

import { useEffect, useState } from "react";
import { pingGateway, BASE_URL } from "@/lib/api";

type Status = "checking" | "online" | "offline";

export default function StatusStrip() {
  const [status, setStatus] = useState<Status>("checking");

  useEffect(() => {
    let cancelled = false;
    async function check() {
      setStatus("checking");
      const ok = await pingGateway();
      if (!cancelled) setStatus(ok ? "online" : "offline");
    }
    check();
    const interval = setInterval(check, 15000);
    return () => {
      cancelled = true;
      clearInterval(interval);
    };
  }, []);

  const label =
    status === "checking" ? "checking" : status === "online" ? "online" : "unreachable";

  return (
    <div className="status-strip">
      <span className="status-strip__brand">APIFLOW</span>
      <span className="status-strip__sep">/</span>
      <span className="status-strip__target" title={BASE_URL}>
        {BASE_URL.replace(/^https?:\/\//, "")}
      </span>
      <span className={`status-strip__dot status-strip__dot--${status}`} />
      <span className="status-strip__label">gateway: {label}</span>

      <style jsx>{`
        .status-strip {
          display: flex;
          align-items: center;
          gap: 10px;
          padding: 10px 20px;
          border-bottom: 1px solid var(--line);
          font-family: var(--font-mono);
          font-size: 12px;
          letter-spacing: 0.04em;
          color: var(--muted);
          background: var(--panel);
          overflow-x: auto;
          white-space: nowrap;
        }
        .status-strip__brand {
          color: var(--text);
          font-weight: 700;
        }
        .status-strip__sep {
          color: var(--muted-dim);
        }
        .status-strip__target {
          color: var(--muted);
        }
        .status-strip__label {
          margin-left: auto;
          color: var(--muted);
        }
        .status-strip__dot {
          width: 7px;
          height: 7px;
          border-radius: 50%;
          margin-left: 8px;
          flex-shrink: 0;
        }
        .status-strip__dot--checking {
          background: var(--signal-pending);
          animation: pulse 1.2s ease-in-out infinite;
        }
        .status-strip__dot--online {
          background: var(--signal);
          box-shadow: 0 0 0 3px var(--signal-dim);
        }
        .status-strip__dot--offline {
          background: var(--signal-down);
          box-shadow: 0 0 0 3px var(--signal-down-dim);
        }
        @keyframes pulse {
          0%,
          100% {
            opacity: 1;
          }
          50% {
            opacity: 0.35;
          }
        }
      `}</style>
    </div>
  );
}
