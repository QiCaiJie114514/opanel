import { toast } from "sonner";
import { apiUrl, sendDeleteRequest, toastError } from "@/lib/api";

export async function downloadLog(name: string) {
  window.open(`${apiUrl}/api/logs/${name}/download`, "_blank");
}

export async function deleteLog(name: string) {
  try {
    await sendDeleteRequest(`/api/logs/${name}`);
    toast.success("删除成功");
  } catch (e: any) {
    toastError(e, "无法删除日志", [
      [400, "请求参数错误"],
      [401, "未登录"],
      [403, "当前日志不可删除"],
      [404, "找不到该日志"]
    ]);
  }
}
