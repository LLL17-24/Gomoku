import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

// 游戏逻辑和控制器
public class GomokuGame
{
    public AIMode aiMode = AIMode.MINIMAX;
    public GomokuLLMAI llmAI;
    private int count = 1;
    protected Board board;
    protected int currentPlayer;
    private boolean isGameOver;
    private int winPlayer = 0;
    protected GomokuAI ai;
    protected int Gamemode = 0;
    private Stack<Move> moveHistory = new Stack<>();
    private LinkedList<LinkedList<Step>> stepHistorys = new LinkedList<>();

    protected static final int EMPTY = 0;
    protected static final int BLACK = 1;
    protected static final int WHITE = 2;
    protected static final int WIN_COUNT = 5;

    static class Move
    {
        int x, y;
        Move(int x, int y)
        {
            this.x = x;
            this.y = y;
        }
    }
    static class Step
    {
        int x,y,color;
        Step(int x, int y, int color)
        {
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }
    public GomokuGame()
    {
        board = new Board();
        this.ai = new GomokuAI(6, 2);
        llmAI = new GomokuLLMAI(ai.aiColor); // ← 只补这一行
        startGame();
    }
    public void startGame()
    {
        board.reset();
        currentPlayer = BLACK;
        isGameOver = false;
        winPlayer = EMPTY;
        LinkedList<Step> currentRoundSteps = new LinkedList<>();
        stepHistorys.add(currentRoundSteps);
        this.count = stepHistorys.size() - 1;
    }
    public boolean placeMove(int r, int c)
    {
        if (isGameOver || board.isOccupied(r, c))
            return false;

        if (board.placePiece(r, c, currentPlayer))
        {
            stepHistorys.get(count).add(new Step(r, c,currentPlayer));

            if (checkWin(board.getLastRow(), board.getLastCol()))
            {
                isGameOver = true;
                winPlayer = currentPlayer;
            }
            else if (checkDraw())
            {
                isGameOver = true;
                winPlayer = EMPTY;
            }
            else
            {
                switchPlayer();
            }
            moveHistory.push(new Move(r, c));
            return true;
        }
        return false;
    }
    private void switchPlayer()
    {
        currentPlayer = (currentPlayer == BLACK) ? WHITE : BLACK;
    }
    public void Changmode()
    {
        this.Gamemode = 1-Gamemode;
    }
    public void Changcolor()
    {
        this.ai.aiColor = 3 - this.ai.aiColor;
        this.llmAI = new GomokuLLMAI(ai.aiColor); // ← 补同步
    }

    public void regret()
    {
        board.lastRow = -1;
        if (moveHistory.isEmpty()) return;
        if (Gamemode == 1)
        {
            Move last = moveHistory.pop();
            board.grid[last.x][last.y] = 0;
            currentPlayer = 3 - currentPlayer;
            stepHistorys.get(count).removeLast();
        }
        else
        {
            if (moveHistory.size() >= 2)
            {
                Move aiMove = moveHistory.pop();
                board.grid[aiMove.x][aiMove.y] = 0;
                Move playerMove = moveHistory.pop();
                board.grid[playerMove.x][playerMove.y] = 0;
                stepHistorys.get(count).removeLast();
                stepHistorys.get(count).removeLast();
            }
        }
        isGameOver = false;
    }
    //回放
    // 提供给 GUI 获取历史记录的方法
    public LinkedList<Step> getstepHistory(int count)
    {
        return new LinkedList<>(stepHistorys.get(count)); // 返回副本，防止外部修改
    }
    //全部棋局
    public LinkedList<LinkedList<Step>>  getStepHistorys()
    {
        return stepHistorys;
    }
    // 清空棋盘但不清空历史的方法（回放前调用）
    public void clearBoardForReplay()
    {
        for (int i = 0; i < board.getSize(); i++)
        {
            for (int j = 0; j < board.getSize(); j++)
            {
                board.grid[i][j] = 0;
            }
        }
    }
    public int[] findAIMove()
    {
        // 防止 AI 在人类回合乱下
        if (currentPlayer != ai.aiColor)
        {
            return new int[]{-1, -1};
        }
        if (aiMode == AIMode.MINIMAX)
        {
            return ai.getBestMove(board.getBoard());
        }
        else
        {
            return llmAI.getCandidateMoves(board.getBoard());
        }
    }
    private boolean checkDraw()
    {
        for(int i = 0; i < board.grid.length; i++)
            for(int j = 0; j < board.grid[0].length; j++)
                if(board.grid[i][j] == EMPTY)
                    return false;
        return true;
    }
    private boolean checkWin(int r, int c)
    {
        int player = board.getPiece(r, c);
        int[][] directions = {{0,1},{1,0},{1,1},{1,-1}};
        for (int[] d : directions)
        {
            int count = 1;
            count += countLine(r,c,d[0],d[1],player);
            count += countLine(r,c,-d[0],-d[1],player);
            if (count >= WIN_COUNT) return true;
        }
        return false;
    }
    private int countLine(int r, int c, int dr, int dc, int p)
    {
        int cnt = 0;
        r += dr; c += dc;
        while (r>=0 && r<board.getSize() && c>=0 && c<board.getSize() && board.getPiece(r,c)==p)
        {
            cnt++; r+=dr; c+=dc;
        }
        return cnt;
    }
    public String getPlayerName(int player) {return (player == BLACK) ? "黑方" : (player == WHITE) ? "白方" : "空";}
    public int getCurrentPlayer() { return currentPlayer; }
    public boolean isGameOver() { return isGameOver; }
    public int getWinPlayer() { return winPlayer; }
    public Board getBoard() { return board; }
}
