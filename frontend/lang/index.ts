import zhCN from "@/lang/zh-cn.json";
import enUS from "@/lang/en-us.json";

export type TranslationKey = keyof typeof zhCN;
export type Translations = Record<TranslationKey, string>;

export const languages: Record<string, Translations> = {
  "zh-cn": zhCN,
  "en-us": enUS
};

export type LanguageCode = keyof typeof languages;
