import { Loader2Icon } from "lucide-react";

import { ErrorState } from "@/components/shared/ErrorState";

interface MapStatusProps {
  zoomedOut: boolean;
  filtersNeedZoom: boolean;
  isLoading: boolean;
  isFetching: boolean;
  isEmpty: boolean;
  error: Error | null;
  onRetry: () => void;
}

const chip = "rounded-xl bg-card px-3 py-1.5 text-sm font-medium shadow-sm ring-1 ring-foreground/5";

export function MapStatus(props: MapStatusProps) {
  return (
    <div className="flex flex-col items-center gap-2" aria-live="polite">
      {props.error ? (
        <div className="w-80 max-w-full">
          <ErrorState message={props.error.message} onRetry={props.onRetry} />
        </div>
      ) : null}
      {props.filtersNeedZoom ? (
        <p className={chip}>Filters apply at street level. Zoom in to use them.</p>
      ) : null}
      {!props.zoomedOut && !props.error && props.isLoading ? (
        <p className={`${chip} flex items-center gap-2`}>
          <Loader2Icon className="size-4 animate-spin" aria-hidden="true" /> Loading incidents…
        </p>
      ) : null}
      {!props.zoomedOut && !props.error && !props.isFetching && props.isEmpty ? (
        <p className={chip}>No incidents in this area.</p>
      ) : null}
    </div>
  );
}
