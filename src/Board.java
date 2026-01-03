// Board核心模型
public class Board
{
    private final int SIZE = 15;
    // 0,空, 1,黑, 2,白
    protected int[][] grid;
    //标记最后一个棋子的横坐标
    public int lastRow = -1;
    //标记最后一个棋子的纵坐标
    private int lastCol = -1;
    //初始化
    public Board()
    {
        grid = new int[SIZE][SIZE];
        initialize();//刚开始全部为空
    }
    //刚开始全部为空
    private void initialize()
    {
        for (int i = 0; i < SIZE; i++)
        {
            for (int j = 0; j < SIZE; j++)
            {
                grid[i][j] = 0;
            }
        }
    }
    //获取棋盘上每个位置的状态0，1，2
    public int getPiece(int r, int c)
    {
        if (r >= 0 && r < SIZE && c >= 0 && c < SIZE)
        {
            return grid[r][c];
        }
        return -1; // 超出边界返回-1
    }
    //判断棋盘上的位置是否被占据
    public boolean isOccupied(int r, int c)
    {
        return grid[r][c] != 0;
    }
    //将棋盘上的某个位置设置为1或2，对应玩家下棋动作，成功返回true
    public boolean placePiece(int r, int c, int player)
    {
        if (r >= 0 && r < SIZE && c >= 0 && c < SIZE && grid[r][c] == 0)
        {
            grid[r][c] = player;
            lastRow = r;
            lastCol = c;
            return true;
        }
        return false;
    }
    //返回大小
    public int getSize()
    {
        return SIZE;
    }
    //最后一个棋子的横坐标
    public int getLastRow()
    {
        return lastRow;
    }
    //最后一个棋子的纵坐标
    public int getLastCol()
    {
        return lastCol;
    }

    public int[][] getBoard()
    {
        return grid;
    }
    //再次初始化，对应玩家重新开局动作
    public void reset()
    {
        initialize();
        lastRow = -1;
        lastCol = -1;
    }
}