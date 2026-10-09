import { Eyebrow } from "./Eyebrow";

const METRICS = [
  {
    label: "Map range",
    value: "30 km",
    text: "Heatmap view across a whole city, with street-level detail as you zoom in.",
  },
  {
    label: "Routing",
    value: "Ranked",
    text: "Walking options ordered by how much reported risk they pass, not only by distance.",
  },
  {
    label: "Privacy",
    value: "Anonymous",
    text: "Report without your name. Contact details are never shown to other people.",
  },
  {
    label: "Emergency",
    value: "Alerts",
    text: "A walk started in the mobile app can alert your trusted contacts if it escalates.",
  },
] as const;

export function Metrics() {
  return (
    <section
      aria-label="What SafeWalk offers"
      className="w-full border-b bg-card"
    >
      <dl className="mx-auto grid max-w-7xl sm:grid-cols-2 lg:grid-cols-4">
        {METRICS.map((m, i) => (
          <div
            key={m.label}
            className="space-y-4 border-b px-4 py-10 md:px-8 lg:border-r lg:border-b-0 lg:last:border-r-0"
          >
            <dt>
              <Eyebrow>{`${String(i + 1).padStart(2, "0")} / ${m.label}`}</Eyebrow>
            </dt>
            <dd className="space-y-3">
              <p className="text-4xl font-light tracking-tight">{m.value}</p>
              <p className="text-sm leading-6 text-muted-foreground">
                {m.text}
              </p>
            </dd>
          </div>
        ))}
      </dl>
    </section>
  );
}
