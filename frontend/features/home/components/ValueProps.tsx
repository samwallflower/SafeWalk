import { Eyebrow } from "./Eyebrow";

const PROPS = [
  {
    title: "Live safety map",
    text: "See where incidents cluster, and open any one for details and community votes.",
  },
  {
    title: "Safer routes",
    text: "Compare walking routes and pick the one that passes fewer reported incidents.",
  },
  {
    title: "Community powered",
    text: "Report incidents and vote on others to keep the map honest and up to date.",
  },
  {
    title: "Private by default",
    text: "Post anonymously. Your account only exists to prevent spam and vote abuse.",
  },
] as const;

export function ValueProps() {
  return (
    <section className="w-full border-y bg-card">
      <div className="mx-auto w-full max-w-7xl px-4 py-20 md:px-8">
        <Eyebrow className="mb-4">03 / Principles</Eyebrow>
        <h2 className="mb-12 text-4xl font-light tracking-tight">
          Built for people on foot
        </h2>
        <div className="grid gap-x-10 gap-y-10 sm:grid-cols-2 lg:grid-cols-4">
          {PROPS.map(({ title, text }) => (
            <div
              key={title}
              className="space-y-3 border-t border-foreground pt-4"
            >
              <h3 className="text-lg font-semibold tracking-tight">{title}</h3>
              <p className="text-sm leading-6 text-muted-foreground">{text}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
