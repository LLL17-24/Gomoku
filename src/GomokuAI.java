import java.util.*;

public class GomokuAI
{
    public static final int SIZE = 15;
    private static final int INF = 1_000_000_000;
    private int depth;
    public int aiColor;
    private int oppColor;
    // 棋形分数权重
    private static final int[] PATTERN_SCORES = {
            0,        // 空格
            1,        // 单颗
            10,       // 活二
            100,      // 活三
            1000,     // 冲四
            10000    // 五连
    };
    private int opponentColor;
    static class Move
    {
        int x, y, score;
        Move(int x, int y, int score)
        {
            this.x = x;
            this.y = y;
            this.score = score;
        }
    }
    public GomokuAI(int depth, int aiColor)
    {
        this.depth = depth;
        this.aiColor = aiColor;
        this.oppColor = 3 - aiColor;
    }
    //对外接口
    public int[] getBestMove(int[][] board)
    {
        int bestScore = -INF;
        int[] bestMove = {SIZE / 2, SIZE / 2};
        List<Move> moves = generateMoves(board, aiColor);
        int[][] copyBoard = copyBoard(board);
        sortMoves(moves, true);
        // 限制候选数（速度关键）
        if (moves.size() > 8)
        {
            moves = moves.subList(0, 8);
        }
        for (Move m : moves)
        {
            copyBoard[m.x][m.y] = oppColor;
            if(hasFive(copyBoard,oppColor))
            {
                bestMove[0] = m.x;
                bestMove[1] = m.y;
                return bestMove;
            }
            copyBoard[m.x][m.y] = aiColor;
            if(hasFive(copyBoard,aiColor))
            {
                bestMove[0] = m.x;
                bestMove[1] = m.y;
                return bestMove;
            }
            int score = minimax(copyBoard, depth - 1, -INF, INF, false);
            copyBoard[m.x][m.y] = 0;
            if (score > bestScore)
            {
                bestScore = score;
                bestMove[0] = m.x;
                bestMove[1] = m.y;
            }
        }
        return bestMove;
    }
    //Minimax算法递归
    //alpha表示AI走这条路最少能拿到alpha分，接下来只会更多，beta表示AI如果走这条路最大能拿到beta分，接下来只会更少
    private int minimax(int[][] board, int d, int alpha, int beta, boolean maximizing)
    {
        if (d == 0) return evaluate(board);
        int color = maximizing ? aiColor : oppColor;
        List<Move> moves = generateMoves(board, color);
        sortMoves(moves, maximizing);
        if (moves.size() > 8)
        {
            moves = moves.subList(0, 8);
        }
        if (maximizing)
        {
            int best = -INF;
            for (Move m : moves)
            {
                board[m.x][m.y] = aiColor;
                int score = minimax(board, d - 1, alpha, beta, false);
                board[m.x][m.y] = 0;
                best = Math.max(best, score);
                alpha = Math.max(alpha, score);
                if (beta <= alpha) break;
            }
            return best;
        }
        else
        {
            int best = INF;
            for (Move m : moves)
            {
                board[m.x][m.y] = oppColor;
                int score = minimax(board, d - 1, alpha, beta, true);
                board[m.x][m.y] = 0;
                best = Math.min(best, score);
                beta = Math.min(beta, score);
                if (beta <= alpha) break;
            }
            return best;
        }
    }
    //走法生成
    private List<Move> generateMoves(int[][] board, int color)
    {
        List<Move> moves = new ArrayList<>();
        int[] range = getSearchRange(board);
        for (int i = range[0]; i <= range[1]; i++)
        {
            for (int j = range[2]; j <= range[3]; j++)
            {
                if(hasFive(board,oppColor))
                {
                    moves.add(new Move(i, j, 100000000));
                    return moves;
                }
                if (board[i][j] == 0 && hasNeighbor(board, i, j, 2))
                {
                    board[i][j] = color;
                    int score = evaluate(board);
                    board[i][j] = 0;
                    moves.add(new Move(i, j, score));
                }
            }
        }
        return moves;
    }
    //走法排序
    private void sortMoves(List<Move> moves, boolean maximizing)
    {
        if (maximizing)
        {
            moves.sort((a, b) -> Integer.compare(b.score, a.score));
        }
        else
        {
            moves.sort((a, b) -> Integer.compare(a.score, b.score));
        }
    }
    //判断是否是孤立的单子
    private boolean hasNeighbor(int[][] board, int x, int y, int dist)
    {
        for (int dx = -dist; dx <= dist; dx++)
            for (int dy = -dist; dy <= dist; dy++)
            {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx, ny = y + dy;
                if (nx >= 0 && nx < SIZE && ny >= 0 && ny < SIZE)
                    if (board[nx][ny] != 0) return true;
            }
        return false;
    }
    //主评估整个棋盘
    private int evaluate(int[][] board)
    {
        if (hasFive(board, aiColor)) return INF;
        if (hasFive(board, oppColor)) return -INF;
        int score = 0;
        for (int i = 0; i < SIZE; i++)
        {
            for (int j = 0; j < SIZE; j++)
            {
                if (board[i][j] == aiColor)
                    score += evaluatePosition(board, i, j, aiColor);
                else if (board[i][j] == oppColor)
                    score -= evaluatePosition(board, i, j, oppColor) * 2.5;
            }
        }
        return score;
    }
    //判断是否连5
    private boolean hasFive(int[][] board, int color)
    {
        int[][] dir = {{1,0},{0,1},{1,1},{1,-1}};
        for (int i = 0; i < SIZE; i++)
        {
            for (int j = 0; j < SIZE; j++)
            {
                if (board[i][j] == color)
                {
                    for (int[] d : dir)
                    {
                        int cnt = 1;
                        int x = i + d[0], y = j + d[1];
                        while (x >= 0 && x < SIZE && y >= 0 && y < SIZE && board[x][y] == color)
                        {
                            cnt++;
                            x += d[0];
                            y += d[1];
                        }
                        if (cnt >= 5) return true;
                    }
                }
            }
        }
        return false;
    }
    private int evaluatePosition(int[][] board, int x, int y, int color)
    {
        int Score = 0;
        // 四个方向：右、下、右下、左下（只需要统计半个方向）
        int[][] dir = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
        int opp = 3 - color;
        for (int[] d : dir)
        {
            int dx = d[0], dy = d[1];
            // 避免重复计数，检查反方向是否是同色棋子
            int px_back = x - dx, py_back = y - dy;
            if (px_back >= 0 && px_back < SIZE && py_back >= 0 && py_back < SIZE && board[px_back][py_back] == color)
            {
                continue; // 跳过，非起点
            }
            // 统计连子长度 (cnt) 和正向活口位置
            int cnt = 1;
            int px_forward = x + dx, py_forward = y + dy;
            // 只向正方向扫描
            while (px_forward >= 0 && px_forward < SIZE && py_forward >= 0 && py_forward < SIZE && board[px_forward][py_forward] == color)
            {
                cnt++;
                px_forward += dx;
                py_forward += dy;
            }
            // 确定两端堵塞状态 (b1, b2)
            // b1: 反向端状态判断 (起点前的位置)
            boolean b1_blocked;
            if (px_back < 0 || px_back >= SIZE || py_back < 0 || py_back >= SIZE || board[px_back][py_back] == opp)
            {
                b1_blocked = true;
            }
            else
            {
                b1_blocked = false; // 活口
            }
            // b2: 正向端状态判断 (连子结束后的位置)
            boolean b2_blocked;
            if (px_forward < 0 || px_forward >= SIZE || py_forward < 0 || py_forward >= SIZE || board[px_forward][py_forward] == opp) {
                b2_blocked = true;
            }
            else
            {
                b2_blocked = false; // 豁口
            }
            // 使用 shapeScore 计算分数
            Score += shapeScore(cnt, b1_blocked, b2_blocked);
        }
        return Score;
    }
    // 分配分数
    private int shapeScore(int cnt, boolean b1, boolean b2)
    {
        // 计算活口，如果不是被堵死 (!b)，则算作一个活口
        int open = (!b1 ? 1 : 0) + (!b2 ? 1 : 0);
        // 采用高权重防止防守问题
        if (cnt >= 5) return 100_000_000;         // 五连
        if (cnt == 4)
        {
            if (open == 2) return 10_000_000;    // 活四
            if (open == 1) return 1_000_000;     // 冲四
            return 0;
        }
        if (cnt == 3)
        {
            if (open == 2) return 10_000;        // 活三
            if (open == 1) return 100;           // 眠三
            return 0;
        }
        if (cnt == 2)
        {
            if (open == 2) return 10;            // 活二
            if (open == 1) return 1;             // 眠二
            return 0;
        }
        return 0;
    }
    //搜索范围
    private int[] getSearchRange(int[][] board)
    {
        int minX = SIZE, maxX = -1, minY = SIZE, maxY = -1;
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (board[i][j] != 0)
                {
                    minX = Math.min(minX, i);
                    maxX = Math.max(maxX, i);
                    minY = Math.min(minY, j);
                    maxY = Math.max(maxY, j);
                }
        if (maxX == -1) return new int[]{0, SIZE-1, 0, SIZE-1};
        minX = Math.max(0, minX - 2);
        minY = Math.max(0, minY - 2);
        maxX = Math.min(SIZE - 1, maxX + 2);
        maxY = Math.min(SIZE - 1, maxY + 2);
        return new int[]{minX, maxX, minY, maxY};
    }
    protected int[][] copyBoard(int[][] board)
    {
        int[][] copy = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            System.arraycopy(board[i], 0, copy[i], 0, SIZE);
        return copy;
    }
    /**
     * ⭐ 从 LLM 给出的候选点中，用 Minimax 选最优
     */
    public int[] getBestMoveFromCandidates(int[][] board, List<int[]> candidates)
    {
        int bestScore = -INF;
        int[] bestMove = candidates.get(0);

        for (int[] p : candidates)
        {
            int x = p[0];
            int y = p[1];

            if (board[x][y] != 0) continue;

            board[x][y] = aiColor;
            int score = minimax(board, depth - 1, -INF, INF, false);
            board[x][y] = 0;

            if (score > bestScore)
            {
                bestScore = score;
                bestMove = p;
            }
        }
        return bestMove;
    }

}
