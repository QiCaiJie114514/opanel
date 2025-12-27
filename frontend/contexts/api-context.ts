import type { APIResponse, InfoResponse, MonitorResponse, VersionResponse } from "@/lib/types";
import React from "react";

function getAPIContext<R>() {
  const ctx = React.createContext<APIResponse<R> | undefined>(undefined);
  ctx.displayName = "APIContext";
  return ctx;
}

export const VersionContext = getAPIContext<VersionResponse>();
export const InfoContext = getAPIContext<InfoResponse>();

export const MonitorContext = React.createContext<MonitorResponse[]>(undefined!);
MonitorContext.displayName = "APIContext";
