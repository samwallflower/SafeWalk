const STEPS = [
  {
    title: "See the street",
    text: "Every report from people nearby sits on one map, each as its own dot. Tap one to see what happened and how recently.",
  },
  {
    title: "Choose your way",
    text: "Plan a route and compare options by distance, time and how many recent reports they pass.",
  },
  {
    title: "Walk with a buddy",
    text: "Start a walk in the mobile app and SafeWalk follows along. Stop for too long or leave the route and it checks in, then alerts your contacts.",
  },
] as const;

export function HowItWorks() {
  return (
    <section className="mx-auto w-full max-w-7xl space-y-8 px-4 py-16 md:px-6">
      <h2 className="max-w-2xl text-4xl leading-[1.05] font-extrabold md:text-5xl">
        Three steps from &quot;hmm&quot; to &quot;home safe&quot;.
      </h2>
      <ol className="grid gap-5 md:grid-cols-3">
        {STEPS.map((step, i) => (
          <li
            key={step.title}
            className="space-y-3 rounded-xl border border-[#e8a317] bg-card p-7"
          >
            <p className="font-heading text-5xl leading-none font-extrabold text-[#e8a317]" aria-hidden="true">
              {i + 1}
            </p>
            <h3 className="text-xl font-bold">{step.title}</h3>
            <p className="leading-7 text-muted-foreground">{step.text}</p>
          </li>
        ))}
      </ol>
    </section>
  );
}
