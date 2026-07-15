const BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export type AuthResponse = {
  accessToken: string;
  refreshToken?: string;
};

export type ApiError = {
  status: number;
  message: string;
};

async function request<T>(path: string, body: unknown): Promise<T> {
  let res: Response;
  try {
    res = await fetch(`${BASE_URL}${path}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
  } catch {
    const err: ApiError = {
      status: 0,
      message: "Can't reach the gateway. Check the API is running and NEXT_PUBLIC_API_URL is set.",
    };
    throw err;
  }

  const text = await res.text();
  const data = text ? JSON.parse(text) : {};

  if (!res.ok) {
    const err: ApiError = {
      status: res.status,
      message: data?.message || data?.error || "Request failed.",
    };
    throw err;
  }

  return data as T;
}

export function login(email: string, password: string) {
  return request<AuthResponse>("/api/v1/auth/login", { email, password });
}

export function register(fullName: string, email: string, password: string) {
  return request<AuthResponse>("/api/v1/auth/register", {
    fullName,
    email,
    password,
  });
}

// Pings the gateway root so the status strip reflects a real, current connection
// rather than a decorative indicator.
export async function pingGateway(): Promise<boolean> {
  try {
    const res = await fetch(`${BASE_URL}/actuator/health`, { method: "GET" });
    return res.ok;
  } catch {
    try {
      // Fall back to root path if actuator isn't exposed publicly
      await fetch(`${BASE_URL}/`, { method: "GET", mode: "no-cors" });
      return true;
    } catch {
      return false;
    }
  }
}

export { BASE_URL };
