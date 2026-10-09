import { Eyebrow } from "./Eyebrow";

const STEPS = [
  {
    title: "Pick where you are going",
    text: "Search a start and a destination anywhere, or use your current location.",
  },
  {
    title: "Compare the routes",
    text: "See each walking option side by side, ranked by how safe it is, not just how short.",
  },
  {
    title: "Walk, and help others",
    text: "Report what you notice and vote on reports so the map stays accurate for everyone.",
  },
] as const;

export function HowItWorks() {
  return (
    <section className="mx-auto w-full max-w-7xl px-4 py-20 md:px-8">
      <div className="mb-12 max-w-2xl space-y-4">
        <Eyebrow>02 / How it works</Eyebrow>
        <h2 className="text-4xl font-light tracking-tight">
          Three steps from &quot;is this street okay?&quot; to a walk you feel
          good about.
        </h2>
      </div>
      <ol className="grid border-y md:grid-cols-3">
        {STEPS.map((step, i) => (
          <li
            key={step.title}
            className="space-y-3 border-b py-8 last:border-b-0 md:border-r md:border-b-0 md:px-8 md:first:pl-0 md:last:border-r-0 md:last:pr-0"
          >
            <p className="font-semibold text-xs tracking-[0.16em] text-primary">
              {String(i + 1).padStart(2, "0")}
            </p>
            <h3 className="text-xl font-semibold tracking-tight">
              {step.title}
            </h3>
            <p className="text-sm leading-6 text-muted-foreground">
              {step.text}
            </p>
          </li>
        ))}
      </ol>
    </section>
  );
}
