import { cn } from "@/lib/utils";

export function FilesEditor({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <div
      className={cn("h-full border rounded-md flex max-lg:flex-col overflow-hidden", className)}
      {...props}/>
  );
}

export function FilesEditorSidebar({ className, ...props }: React.ComponentProps<"aside">) {
  return (
    <aside
      className={cn("flex-1/4 h-fit max-h-full max-lg:max-h-none flex flex-col gap-3 p-2", className)}
      {...props}/>
  );
}

export function FilesEditorSidebarList({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <div
      className={cn("flex-1 flex flex-col overflow-y-auto", className)}
      {...props}/>
  );
}

export function FilesEditorSidebarListItem({
  className,
  isActive = false,
  ...props
}: React.ComponentProps<"div"> & {
  isActive?: boolean
}) {
  return (
    <div
      className={cn("h-10 rounded-sm px-3 py-1 flex items-center gap-2 text-sm cursor-pointer [&_svg]:size-4", isActive && "bg-muted", className)}
      {...props}/>
  );
}

export function FilesEditorContent({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <div
      className={cn("flex-3/4 border-l max-lg:border-l-0 max-lg:border-t max-lg:min-h-96 flex flex-col justify-end max-lg:flex-col-reverse", className)}
      {...props}/>
  );
}

export function FilesEditorStatusBar({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <div
      className={cn("h-6 px-2 border-t max-lg:border-t-0 max-lg:border-b flex items-center [&>*]:text-xs [&>*]:text-muted-foreground cursor-default", className)}
      {...props}/>
  );
}

export function FilesEditorStatusBarItem({
  className,
  side = "left",
  ...props
}: React.ComponentProps<"span"> & {
  side?: "left" | "right"
}) {
  return (
    <span
      className={cn(side === "right" && "ml-auto", className)}
      {...props}/>
  );
}
