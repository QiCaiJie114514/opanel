import type { Editor, OnMount } from "@monaco-editor/react";
import type { ServerGamerules } from "./gamerules";

export type APIResponse<T> = {
  code: number
  error: string
} & T;

export type ServerType = "Bukkit" | "Spigot" | "Paper" | "Fabric" | "Forge" | "Neoforge" | "Folia";

export enum GameMode {
  ADVENTURE = "adventure",
  SURVIVAL = "survival",
  CREATIVE = "creative",
  SPECTATOR = "spectator"
}

export interface Save {
  name: string
  displayName: string // base64
  path: string
  size: number
  isRunning: boolean
  isCurrent: boolean
  defaultGameMode: GameMode
}

export interface Player {
  name: string
  uuid: string
  isOnline: boolean
  isOp: boolean
  isBanned: boolean
  gamemode: GameMode
  banReason?: string // base64
  isWhitelisted?: boolean
  ping?: number
}

/** Bot player may not have a name */
export interface UnnamedPlayer extends Player {
  name: never
}

export type Whitelist = {
  name: string
  uuid: string
}[]

export type EditorRefType = Parameters<OnMount>[0];
export type EditorOptionsType = React.ComponentProps<typeof Editor>["options"];

export enum AvatarProvider {
  MINOTAR = "https://minotar.net/avatar/",
  MINEATAR = "https://api.mineatar.io/face/",
  MCHEADS = "https://api.mcheads.org/head/",
  /** @see https://github.com/crafatar/crafatar/issues/329#issuecomment-3559253664 */
  CRAFATAR = "https://avatars.cloudhaven.gg/avatars/"
}

export enum SkinProvider {
  MINOTAR = "https://minotar.net/skin/",
  MINEATAR = "https://api.mineatar.io/skin/",
  MCHEADS = "https://api.mcheads.org/skin/",
  /** @see https://github.com/crafatar/crafatar/issues/329#issuecomment-3559253664 */
  CRAFATAR = "https://avatars.cloudhaven.gg/skins/"
}

export enum CapeProvider {
  /** @see https://github.com/crafatar/crafatar/issues/329#issuecomment-3559253664 */
  CRAFATAR = "https://avatars.cloudhaven.gg/capes/"
}

/** `/api/version` */
export interface VersionResponse {
  serverType: ServerType
  version: string
}

/** `/api/info` */
export interface InfoResponse {
  favicon: string | null
  motd: string // base64
  port: number
  maxPlayerCount: number
  whitelist: boolean
  uptime: number
  ingameTime: {
    current: number
    doDaylightCycle: boolean
    paused: boolean
    mspt: number
  }
}

/** `/api/monitor` */
export interface MonitorResponse {
  mem: number
  cpu: number
  tps: number
}

/** `/api/control/properties` */
export interface ServerPropertiesResponse {
  properties: string // base64
}

/** `/api/control/code-of-conduct` */
export interface CodeOfConductResponse {
  codeOfConducts: Record<string, string> // base64
}

/** `/api/gamerules` */
export interface GamerulesResponse {
  gamerules: ServerGamerules
}

/** `/api/logs` */
export interface LogsResponse {
  logs: string[]
}

/** `/api/saves` */
export interface SavesResponse {
  saves: Save[]
}

/** `/api/saves/{saveName}` */
export interface DownloadSaveResponse {
  download: string
}

/** `/api/players` */
export interface PlayersResponse {
  maxPlayerCount: number
  whitelist: boolean
}

/** `/api/whitelist` */
export interface WhitelistResponse {
  whitelist: Whitelist
}

/** `/api/banned-ips` */
export interface BannedIpsResponse {
  bannedIps: string[]
}
