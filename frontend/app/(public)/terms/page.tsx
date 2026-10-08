import type { Metadata } from "next";

import { LegalPage } from "@/features/legal/components/LegalPage";

export const metadata: Metadata = { title: "Terms — SafeWalk" };

export default function TermsPage() {
  return (
    <LegalPage title="Terms of use" description="Please read before using SafeWalk.">
      <p>
        SafeWalk is provided as part of a university thesis, as is and without warranty. Safety
        scores and routes are estimates based on user reports and may be incomplete or wrong.
      </p>
      <h2>Your reports</h2>
      <p>
        Report only incidents you have witnessed or experienced. Do not post personal information
        about others. Reports may be hidden or removed by moderators.
      </p>
      <h2>Emergencies</h2>
      <p>Do not rely on SafeWalk in an emergency. Contact local emergency services directly.</p>
    </LegalPage>
  );
}
