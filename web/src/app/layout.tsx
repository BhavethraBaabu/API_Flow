import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "APIFlow — Sign in",
  description: "Auth console for the APIFlow reliability monitoring platform.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
