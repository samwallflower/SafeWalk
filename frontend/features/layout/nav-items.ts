export interface NavItem {
  href: string;
  label: string;
}

export const NAV_ITEMS: readonly NavItem[] = [
  { href: "/map", label: "Explore Map" },
  { href: "/reports", label: "Reports" },
  { href: "/plan", label: "Plan a route" },
  { href: "/about", label: "About" },
];
