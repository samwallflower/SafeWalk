import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";

afterEach(() => {
  vi.unstubAllEnvs();
  vi.resetModules();
});

async function loadButtons() {
  const mod = await import("./DemoLoginButtons");
  return mod.DemoLoginButtons;
}

describe("DemoLoginButtons", () => {
  it("renders nothing when demo login is disabled", async () => {
    vi.stubEnv("NEXT_PUBLIC_DEMO_LOGIN_ENABLED", "false");
    const DemoLoginButtons = await loadButtons();
    const { container } = render(<DemoLoginButtons onFill={vi.fn()} />);
    expect(container).toBeEmptyDOMElement();
  });

  it("fills credentials without submitting a form", async () => {
    vi.stubEnv("NEXT_PUBLIC_DEMO_LOGIN_ENABLED", "true");
    vi.stubEnv("NEXT_PUBLIC_DEMO_USER_EMAIL", "user@demo.test");
    vi.stubEnv("NEXT_PUBLIC_DEMO_USER_PASSWORD", "pw");
    const DemoLoginButtons = await loadButtons();
    const onFill = vi.fn();
    const onSubmit = vi.fn((e: { preventDefault: () => void }) => e.preventDefault());
    render(
      <form onSubmit={onSubmit}>
        <DemoLoginButtons onFill={onFill} />
      </form>,
    );
    await userEvent.click(screen.getByRole("button", { name: "Use demo user" }));
    expect(onFill).toHaveBeenCalledWith({ email: "user@demo.test", password: "pw" });
    expect(onSubmit).not.toHaveBeenCalled();
  });
});
