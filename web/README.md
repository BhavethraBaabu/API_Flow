# APIFlow Web

Login/register console for the APIFlow gateway (`authentication-service` + `gateway-service`).

## Local dev

```bash
npm install
cp .env.example .env.local   # point NEXT_PUBLIC_API_URL at your gateway
npm run dev
```

## Deploy to Vercel

1. Push this folder to a GitHub repo (or a subfolder of `API_Flow` with Root Directory set accordingly in Vercel).
2. Import the repo at https://vercel.com/new.
3. Set the environment variable `NEXT_PUBLIC_API_URL` to your deployed gateway's public URL (Render/Railway/Fly.io) — must be HTTPS and reachable from the browser.
4. Deploy. Framework preset "Next.js" is auto-detected.

The status strip at the top of the page pings `NEXT_PUBLIC_API_URL` directly, so the backend must have CORS enabled for the Vercel domain and (ideally) expose `/actuator/health`.
