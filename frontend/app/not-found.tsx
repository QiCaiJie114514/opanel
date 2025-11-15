"use client";

import Link from "next/link";
import { Button } from "@/components/ui/button";
import { googleSansCode } from "@/lib/fonts";
import { cn } from "@/lib/utils";
import { copyrightInfo } from "@/lib/global";

export default function NotFound() {
  return (
    <div className="w-screen h-screen flex justify-center items-center">
      <div className="text-center space-y-4">
        <h1 className={cn("text-9xl text-muted-foreground", googleSansCode.className)}>{"{404}"}</h1>
        <p className="text-lg">你访问的路径不存在</p>
        <div className="mt-10 space-x-2 [&>*]:cursor-pointer">
          <Button asChild>
            <Link href="/">面板首页</Link>
          </Button>
          <Button
            variant="outline"
            onClick={() => history.go(-1)}>
            返回上一页
          </Button>
        </div>
        <span className="text-sm text-muted-foreground">{copyrightInfo}</span>
      </div>
    </div>
  );
}
