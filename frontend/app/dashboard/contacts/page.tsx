import type { Metadata } from "next";

import { SectionPanel } from "@/components/shared/SectionPanel";
import { ContactsManager } from "@/features/contacts/components/ContactsManager";

export const metadata: Metadata = { title: "Contacts — SafeWalk" };

export default function Page() {
  return (
    <SectionPanel title="Emergency contacts">
      <ContactsManager />
    </SectionPanel>
  );
}
