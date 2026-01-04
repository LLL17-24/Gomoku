// MoveGenerator.java
// 说明：仅生成靠近已有棋子的候选格，减少分支。
// 配置：radius（默认 2），maxCandidates（可限制生成数量）

import java.util.*;

public class MoveGenerator {
    private final int radius;
    private final int maxCandidates;

    public MoveGenerator(int radius, int maxCandidates) {
        this.radius = radius;
        this.maxCandidates = maxCandidates;
    }

    public List<int[]> generate(Board board, int player) {
        int w = board.getWidth(), h = board.getHeight();
        boolean[][] mark = new boolean[w][h];
        List<int[]> candidates = new ArrayList<>();

        // 收集所有已落子点
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (board.get(x,y) != 0) {
                    // 标记周围 radius 范围内的空位
                    for (int dx = -radius; dx <= radius; dx++) {
                        for (int dy = -radius; dy <= radius; dy++) {
                            int nx = x + dx, ny = y + dy;
                            if (board.inBounds(nx, ny) && board.get(nx, ny) == 0 && !mark[nx][ny]) {
                                mark[nx][ny] = true;
                                candidates.add(new int[] { nx, ny });
                            }
                        }
                    }
                }
            }
        }

        // 如果没有任何棋子（开局），可选择中心或整个棋盘一部分
        if (candidates.isEmpty()) {
            int cx = w / 2, cy = h / 2;
            candidates.add(new int[]{cx, cy});
        }

        // 可选：限制候选数（按简单 heuristic 随机/评估排序）
        if (candidates.size() > maxCandidates) {
            // 随机挑选 top N（也可以基于评估器排序）
            Collections.shuffle(candidates);
            return candidates.subList(0, maxCandidates);
        }
        return candidates;
    }
}
