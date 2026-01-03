import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GomokuLLMAI
{
    private int aiColor;
    private static final String API_KEY = "sk-a168e98fb0424f3c9a0980eae3916fc7";
    private static final String API_ENDPOINT = "https://api.deepseek.com/v1/chat/completions";
    public GomokuLLMAI(int aiColor)
    {
        this.aiColor = aiColor;
    }
    // 将棋盘数组转换为 AI 易读的点阵字符
    private String renderBoardText(int[][] grid)
    {
        StringBuilder sb = new StringBuilder();
        // 1.添加顶部列号
        sb.append("   "); // 留出行号的空白
        for (int i = 0; i < grid[0].length; i++)
        {
            sb.append(String.format("%2d", i)); // 保持两位宽度对齐
        }
        sb.append("\n");
        // 2.遍历棋盘，每行开头添加行号
        for (int r = 0; r < grid.length; r++)
        {
            sb.append(String.format("%2d ", r)); // 打印行号
            for (int c = 0; c < grid[r].length; c++)
            {
                if (grid[r][c] == 1) sb.append(" 1"); // 加上空格，增强区分度
                else if (grid[r][c] == 2) sb.append(" 2");
                else sb.append(" 0");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public int[] getCandidateMoves(int[][] grid)
    {
        String boardStr = renderBoardText(grid);
        // 动态生成已占用位置列表
        StringBuilder occupied = new StringBuilder();
        for (int r = 0; r < grid.length; r++)
        {
            for (int c = 0; c < grid[r].length; c++)
            {
                if (grid[r][c] != 0)
                {
                    occupied.append("(").append(r).append(",").append(c).append(") ");
                }
            }
        }
        String roleStr = (aiColor == 1) ? "黑棋(1)" : "白棋(2)";
        String prompt = "你是五子棋高手。以下是当前 15x15 棋盘：" +
                "\n" + boardStr +
                "\n1代表黑棋，2代表白棋"+
                "\n【禁止落子点】：以下坐标已有棋子，绝对不能下在这里：" + occupied.toString() +
                "\n你是 " + roleStr + "。请根据棋势（连五、活四、冲四、活三等）给出一个最佳落子坐标。" +
                "\n一定要根据五子棋的规则找出一个最佳路线！！！"+
                "\n输出格式：仅输出坐标，如 (行,列)，不要任何解释。";
        System.out.println(prompt);
        String response = callDeepSeek(prompt);
        return parseCoordinates(response);
    }
    private String callDeepSeek(String content)
    {
        try
        {
            URL url = new URL(API_ENDPOINT);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);


            String safeContent = content
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n");

            String requestBody =
                    "{"
                            + "\"model\":\"deepseek-chat\","
                            + "\"messages\":["
                            + " {\"role\":\"user\",\"content\":\"" + safeContent + "\"}"
                            + "]"
                            + "}";

            try (OutputStream os = conn.getOutputStream())
            {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            InputStream is = (code == 200) ? conn.getInputStream() : conn.getErrorStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder resp = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
            {
                resp.append(line);
            }
            System.out.println("DeepSeek RAW RESPONSE:");
            System.out.println(resp);
            if (code != 200)
            {
                return "";
            }

            // 正确解析 choices[0].message.content
            String marker = "\"content\":\"";
            int start = resp.indexOf(marker);
            if (start == -1) return "";
            start += marker.length();

            int end = start;
            while (end < resp.length())
            {
                if (resp.charAt(end) == '"' && resp.charAt(end - 1) != '\\')
                    break;
                end++;
            }
            String result = resp.substring(start, end)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");

            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }
    private int[] parseCoordinates(String input)
    {
        int[] result = new int[2];
        try
        {
            // 使用正则或简单的字符串处理提取数字
            // 假设 AI 返回 "(7,8)" 或 "7,8"
            String cleaned = input.replaceAll("[^0-9,]", "");
            String[] parts = cleaned.split(",");
            if (parts.length >= 2)
            {
                int r = Integer.parseInt(parts[0]);
                int c = Integer.parseInt(parts[1]);
                result[0] = r;
                result[1] = c;
            }
        }
        catch (Exception e)
        {
            System.err.println("坐标解析失败: " + input);
        }
        return result;
    }

}