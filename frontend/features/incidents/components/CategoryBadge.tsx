import { Chip } from "@/components/shared/Chip";

export function CategoryBadge({ name }: { name: string }) {
  return <Chip tone="danger">{name}</Chip>;
}
