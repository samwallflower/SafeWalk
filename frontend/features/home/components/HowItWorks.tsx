const STEPS = [
  { title: "Pick where you are going", text: "Search a start and a destination anywhere, or use your current location." },
  { title: "Compare the routes", text: "See each walking option side by side, ranked by how safe it is, not just how short." },
  { title: "Walk, and help others", text: "Report what you notice and vote on reports so the map stays accurate for everyone." },
] as const;

export function HowItWorks() {
  return (
    <section className="mx-auto w-full max-w-7xl px-4 py-16 md:px-6">
      <div className="mb-10 max-w-2xl space-y-2">
        <h2 className="text-3xl font-bold tracking-tight">How it works</h2>
        <p className="text-muted-foreground">Three steps from &quot;is this street okay?&quot; to a walk you feel good about.</p>
      </div>
      <ol className="grid gap-6 md:grid-cols-3">
        {STEPS.map((step, i) => (
          <li key={step.title} className="relative space-y-3 rounded-2xl bg-card p-6 shadow-sm ring-1 ring-foreground/5">
            <span className="flex size-10 items-center justify-center rounded-full bg-primary text-lg font-bold text-primary-foreground">{i + 1}</span>
            <h3 className="text-lg font-bold">{step.title}</h3>
            <p className="text-sm leading-6 text-muted-foreground">{step.text}</p>
          </li>
        ))}
      </ol>
    </section>
  );
}
