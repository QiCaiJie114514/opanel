import type { WebSocketClient } from "@/lib/ws";
import { useEffect, useState } from "react";

export function useWebSocket<M extends string, C extends WebSocketClient<M>>(clientClass: new () => C) {
  const [client, setClient] = useState<C | null>(null);

  useEffect(() => {
    const ws = new clientClass();
    setClient((current) => {
      return current ?? ws;
    });
    
    return () => {
      ws.close();
      setClient(null);
    };
  }, [clientClass]);

  return client;
}
