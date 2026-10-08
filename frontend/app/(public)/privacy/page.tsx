import type { Metadata } from "next";

import { LegalPage } from "@/features/legal/components/LegalPage";

export const metadata: Metadata = { title: "Privacy — SafeWalk" };

export default function PrivacyPage() {
  return (
    <LegalPage title="Privacy" description="What SafeWalk stores and why.">
      <h2>Account data</h2>
      <p>We store your email and account details so you can sign in, report and vote.</p>
      <h2>Reports</h2>
      <p>
        Incident reports include a location, category and description. Reports marked anonymous
        never show who submitted them to other users.
      </p>
      <h2>Location</h2>
      <p>
        Your device location is requested only when you choose to use it, such as “Use my
        location”. It is used to centre the map and to prefill report locations.
      </p>
      <h2>Authentication</h2>
      <p>Your sign-in token is kept in a secure cookie that page scripts cannot read.</p>
    </LegalPage>
  );
}
