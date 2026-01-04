// GomokuSearch.java
// 说明：alpha–beta 搜索与迭代加深的实现。
// 依赖：Board 接口需要提供以下方法（若现有 Board 不一致，请写 adapter):
//   int get(int x, int y)            // 0 empty, 1 黑, 2 白
//   boolean isLegal(int x, int y)
//   void makeMove(int x, int y, int player)
//   void undoMove(int x, int y)
//   boolean isWin(int x, int y, int player) // 可选，加速胜负判定
//   int getWidth(), int getHeight()
// 可配置参数在构造器中传入：maxDepth, timeLimitMs, searchRadius

import java.util.*;

public class GomokuSearch {
    private final GomokuEvaluator evaluator;
    private final MoveGenerator moveGen;
    private final int maxDepth;
    private final long timeLimitMs;

    private volatile long stopAt = Long.MAX_VALUE;
    private int bestX = -1, bestY = -1;

    public GomokuSearch(GomokuEvaluator evaluator, MoveGenerator moveGen, int maxDepth, long timeLimitMs) {
        this.evaluator = evaluator;
        this.moveGen = moveGen;
        this.maxDepth = maxDepth;
        this.timeLimitMs = timeLimitMs;
    }

    public int[] findBestMove(Board board, int player) {
        stopAt = System.currentTimeMillis() + timeLimitMs;
        bestX = -1; bestY = -1;

        // Iterative deepening
        for (int depth = 1; depth <= maxDepth; depth++) {
            try {
                alphaBetaRoot(board, depth, player);
            } catch (TimeUpException e) {
                break; // use last best move
            }
            if (System.currentTimeMillis() > stopAt) break;
        }
        return new int[] { bestX, bestY };
    }

    private void alphaBetaRoot(Board board, int depth, int player) {
        List<int[]> moves = moveGen.generate(board, player);
        // Order moves by static score (descending)
        moves.sort((a, b) -> {
            int sa = evaluator.scoreSingleMove(board, a[0], a[1], player);
            int sb = evaluator.scoreSingleMove(board, b[0], b[1], player);
            return Integer.compare(sb, sa);
        });

        int alpha = Integer.MIN_VALUE + 1;
        int beta = Integer.MAX_VALUE - 1;
        int bestVal = alpha;

        for (int[] m : moves) {
            checkTime();
            board.makeMove(m[0], m[1], player);
            int val = -alphaBeta(board, depth - 1, -beta, -alpha, 3 - player);
            board.undoMove(m[0], m[1]);

            if (val > bestVal) {
                bestVal = val;
                bestX = m[0]; bestY = m[1];
            }
            alpha = Math.max(alpha, val);
            if (alpha >= beta) break; // beta cutoff
        }
    }

    private int alphaBeta(Board board, int depth, int alpha, int beta, int player) {
        checkTime();
        if (depth == 0) {
            return evaluator.evaluate(board, player);
        }

        List<int[]> moves = moveGen.generate(board, player);
        if (moves.isEmpty()) return evaluator.evaluate(board, player);

        // Move ordering heuristic
        moves.sort((a, b) -> {
            int sa = evaluator.scoreSingleMove(board, a[0], a[1], player);
            int sb = evaluator.scoreSingleMove(board, b[0], b[1], player);
            return Integer.compare(sb, sa);
        });

        for (int[] m : moves) {
            board.makeMove(m[0], m[1], player);
            int val = -alphaBeta(board, depth - 1, -beta, -alpha, 3 - player);
            board.undoMove(m[0], m[1]);

            if (val > alpha) alpha = val;
            if (alpha >= beta) {
                return alpha; // cutoff
            }
        }
        return alpha;
    }

    private void checkTime() {
        if (System.currentTimeMillis() > stopAt) throw new TimeUpException();
    }

    private static class TimeUpException extends RuntimeException {}
}
