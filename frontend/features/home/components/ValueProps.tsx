import { LockKeyholeIcon, MapIcon, RouteIcon, UsersIcon } from "lucide-react";

const PROPS = [
  { icon: MapIcon, title: "Live safety map", text: "See where incidents cluster, and open any one for details and community votes." },
  { icon: RouteIcon, title: "Safer routes", text: "Compare walking routes and pick the one that passes fewer reported incidents." },
  { icon: UsersIcon, title: "Community powered", text: "Report incidents and vote on others to keep the map honest and up to date." },
  { icon: LockKeyholeIcon, title: "Private by default", text: "Post anonymously. Your account only exists to prevent spam and vote abuse." },
] as const;

export function ValueProps() {
  return (
    <section className="w-full border-y bg-card">
      <div className="mx-auto w-full max-w-7xl px-4 py-16 md:px-6">
        <h2 className="mb-10 text-3xl font-bold tracking-tight">Built for people on foot</h2>
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {PROPS.map(({ icon: Icon, title, text }) => (
            <div key={title} className="space-y-3">
              <span className="flex size-11 items-center justify-center rounded-xl bg-info-soft text-primary">
                <Icon className="size-5" aria-hidden="true" />
              </span>
              <h3 className="text-lg font-bold">{title}</h3>
              <p className="text-sm leading-6 text-muted-foreground">{text}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
