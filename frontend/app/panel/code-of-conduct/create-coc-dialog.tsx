import { useCallback, useState, type PropsWithChildren } from "react";
import { toast } from "sonner";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Field, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { validateLocaleCode } from "@/lib/utils";

export function CreateCodeOfConductDialog({
  excludedLocales,
  children,
  asChild,
  onAction
}: PropsWithChildren & {
  excludedLocales: string[]
  asChild?: boolean,
  onAction?: (lang: string) => void
}) {
  const [open, setOpen] = useState<boolean>(false);
  const [inputtedLang, setInputtedLang] = useState<string>("");

  const handleAction = useCallback(() => {
    const formattedLang = inputtedLang.toLowerCase().replaceAll("-", "_");
    if(formattedLang === "" || !validateLocaleCode(formattedLang)) {
      toast.error("请输入有效的语言代码，如：zh_cn");
      return;
    }
    if(excludedLocales.includes(formattedLang)) {
      toast.error("你所要创建的语言版本已存在");
      return;
    }
    
    onAction && onAction(formattedLang);
    setOpen(false);
  }, [excludedLocales, inputtedLang, onAction]);

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild={asChild}>{children}</DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>新建行为准则文档</DialogTitle>
          <DialogDescription>
            在此新建一个特定语言的行为准则文档。
          </DialogDescription>
        </DialogHeader>
        <div>
          <Field>
            <FieldLabel>语言</FieldLabel>
            <Input
              value={inputtedLang}
              placeholder="请输入语言代码...（如：zh_cn）"
              onInput={(e) => setInputtedLang((e.target as HTMLInputElement).value)}/>
          </Field>
        </div>
        <DialogFooter>
          <DialogClose asChild>
            <Button variant="outline">取消</Button>
          </DialogClose>
          <Button
            disabled={inputtedLang === ""}
            onClick={() => handleAction()}>
            新建
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
