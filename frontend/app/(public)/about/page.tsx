import type { Metadata } from "next";

import { LegalPage } from "@/features/legal/components/LegalPage";

export const metadata: Metadata = { title: "About — SafeWalk" };

export default function AboutPage() {
  return (
    <LegalPage title="About SafeWalk" description="Safety-aware navigation for pedestrians.">
      <p>
        SafeWalk combines crowd-sourced incident reports into a heatmap and recommends walking
        routes that avoid higher-risk areas. It works in any city, for everyone.
      </p>
      <h2>Thesis project</h2>
      <p>
        SafeWalk is a university thesis project. It is not a substitute for contacting emergency
        services. In an emergency, call your local emergency number.
      </p>
    </LegalPage>
  );
}
