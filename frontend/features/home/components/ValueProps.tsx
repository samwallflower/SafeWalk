import { ClockIcon, NavigationIcon, PhoneIcon, TriangleAlertIcon } from "lucide-react";

const PROPS = [
  { title: "Idle check-in", text: "Are you safe? Tap once to say yes.", Icon: ClockIcon },
  { title: "Off-route notice", text: "A nudge when you drift from your path.", Icon: NavigationIcon },
  { title: "Hold-to-send SOS", text: "One second hold, no accidental alarms.", Icon: TriangleAlertIcon },
  { title: "Local numbers", text: "Police and ambulance for where you are.", Icon: PhoneIcon },
] as const;

export function ValueProps() {
  return (
    <section className="w-full bg-accent">
      <div className="mx-auto w-full max-w-7xl space-y-12 px-4 py-20 md:px-6 md:py-24">
        <div className="max-w-4xl space-y-5">
          <h2 className="text-5xl leading-none font-extrabold text-balance md:text-7xl">
            Quiet by default. Loud when it matters.
          </h2>
          <p className="max-w-3xl text-xl leading-8 text-muted-foreground">
            A gentle prompt first. If you do not answer, an alarm rings on your phone and your emergency contacts get your location.
          </p>
        </div>
        <ul className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
          {PROPS.map(({ title, text, Icon }) => (
            <li key={title} className="flex min-h-60 flex-col gap-4 rounded-xl border border-primary bg-card p-8">
              <span className="grid size-14 place-items-center rounded-lg bg-leaf text-foreground">
                <Icon className="size-7" aria-hidden="true" />
              </span>
              <h3 className="text-2xl leading-tight font-extrabold">{title}</h3>
              <p className="text-lg leading-7 text-muted-foreground">{text}</p>
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}
