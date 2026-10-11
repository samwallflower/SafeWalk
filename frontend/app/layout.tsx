import type { Metadata } from "next";
import { Bricolage_Grotesque, Figtree } from "next/font/google";

import { Toaster } from "@/components/ui/sonner";
import { QueryProvider } from "@/components/providers/QueryProvider";
import { Footer } from "@/features/layout/components/Footer";
import { Header } from "@/features/layout/components/Header";

import "./globals.css";

const figtree = Figtree({ variable: "--font-figtree", subsets: ["latin"] });
const bricolage = Bricolage_Grotesque({ variable: "--font-bricolage", subsets: ["latin"], weight: ["500", "700", "800"] });

export const metadata: Metadata = {
  title: { default: "SafeWalk", template: "%s" },
  description:
    "Safety-aware walking navigation powered by crowd-sourced incident reports.",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="en" className={`${figtree.variable} ${bricolage.variable} h-full antialiased`}>
      <body className="flex min-h-full flex-col">
        <a
          href="#main"
          className="sr-only z-50 rounded-md bg-card px-3 py-2 text-sm font-semibold text-primary focus:not-sr-only focus:fixed focus:top-2 focus:left-2"
        >
          Skip to content
        </a>
        <QueryProvider>
          <Header />
          <main
            id="main"
            tabIndex={-1}
            className="flex flex-1 flex-col outline-none"
          >
            {children}
          </main>
          <Footer />
          <Toaster position="top-center" />
        </QueryProvider>
      </body>
    </html>
  );
}
