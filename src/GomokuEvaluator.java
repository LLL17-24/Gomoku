// GomokuEvaluator.java
// 说明：静态评估器。将棋型评估拆成小函数，方便测试与调参。
// 简单实现：以连子数量和开放口判断为主；可扩展为更复杂的 pattern-based evaluator。

public class GomokuEvaluator {
    // 配置参数（可以改成构造器参数）
    private final int FIVE = 1000000;
    private final int FOUR = 10000;
    private final int THREE = 1000;
    private final int TWO = 100;

    // 评估以当前 player 为主的正向分数（较大的分数代表 player 更有利）
    public int evaluate(Board board, int player) {
        // 简要实现：遍历每个点，按方向累加模式分
        int w = board.getWidth(), h = board.getHeight();
        int score = 0;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (board.get(x,y) == player) {
                    score += evaluateFrom(board, x, y, player);
                } else if (board.get(x,y) == 3 - player) {
                    score -= evaluateFrom(board, x, y, 3 - player);
                }
            }
        }
        return score;
    }

    // 评估单步落子的静态价值（用于走子排序）
    public int scoreSingleMove(Board board, int x, int y, int player) {
        board.makeMove(x, y, player);
        int s = evaluate(board, player);
        board.undoMove(x, y);
        return s;
    }

    // 基于从点向四个方向的连子统计，返回估值
    private int evaluateFrom(Board board, int x, int y, int player) {
        int total = 0;
        total += scoreLine(board, x, y, 1, 0, player); // 水平
        total += scoreLine(board, x, y, 0, 1, player); // 垂直
        total += scoreLine(board, x, y, 1, 1, player); // 主对角
        total += scoreLine(board, x, y, 1, -1, player); // 副对角
        return total;
    }

    private int scoreLine(Board board, int x, int y, int dx, int dy, int player) {
        int count = 1;
        int openEnds = 0;
        int nx = x + dx, ny = y + dy;
        while (board.inBounds(nx, ny) && board.get(nx, ny) == player) { count++; nx += dx; ny += dy; }
        if (board.inBounds(nx, ny) && board.get(nx, ny) == 0) openEnds++;

        nx = x - dx; ny = y - dy;
        while (board.inBounds(nx, ny) && board.get(nx, ny) == player) { count++; nx -= dx; ny -= dy; }
        if (board.inBounds(nx, ny) && board.get(nx, ny) == 0) openEnds++;

        if (count >= 5) return FIVE;
        if (count == 4 && openEnds > 0) return FOUR;
        if (count == 3 && openEnds > 0) return THREE;
        if (count == 2 && openEnds > 0) return TWO;
        return 0;
    }
}
