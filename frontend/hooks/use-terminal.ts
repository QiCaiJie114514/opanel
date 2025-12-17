import { useEffect, useState } from "react";
import { TerminalClient } from "@/lib/ws/terminal";

export function useTerminal() {
  const [client, setClient] = useState<TerminalClient | null>(null);

  useEffect(() => {
    const ws = new TerminalClient();
    setClient(ws);
    
    return () => ws.close();
  }, []);

  return client;
}
