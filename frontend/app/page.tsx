import { CtaBand } from "@/features/home/components/CtaBand";
import { Hero } from "@/features/home/components/Hero";
import { HowItWorks } from "@/features/home/components/HowItWorks";
import { ValueProps } from "@/features/home/components/ValueProps";

export default function HomePage() {
  return (
    <>
      <Hero />
      <HowItWorks />
      <ValueProps />
      <CtaBand />
    </>
  );
}
