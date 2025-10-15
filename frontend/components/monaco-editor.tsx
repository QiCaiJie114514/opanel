"use client";

import { useEffect } from "react";
import { Editor, type EditorProps } from "@monaco-editor/react";
import { loader } from "@monaco-editor/react";
import * as monaco from "monaco-editor";

export default function MonacoEditor(props: EditorProps) {
  useEffect(() => {
    if(typeof window === "undefined") return;

    (window as any).MonacoEnvironment = {
      getWorker: (_workerId: never, label: string) => {
        switch(label) {
          case "json":
            return new Worker(
              new URL("monaco-editor/esm/vs/language/json/json.worker", import.meta.url)
            );
          default:
            return new Worker(
              new URL("monaco-editor/esm/vs/editor/editor.worker", import.meta.url)
            );
        }
      }
    };
  }, []);

  loader.config({ monaco });
  return <Editor {...props}/>;
}
