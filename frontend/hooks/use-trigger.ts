import { useState } from "react";

export function useTrigger() {
  const [, setTrigger] = useState(false);

  return {
    trigger() {
      setTrigger((current) => !current);
    }
  };
}
