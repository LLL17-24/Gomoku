import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;


// UI/视图层
public class GomokuGUI extends JFrame
{
    //鼠标开关
    private boolean allowclik = true;
    protected GomokuGame game;
    // 棋盘绘制面板（核心可视化组件）
    protected BoardPanel boardPanel;
    // 游戏状态提示标签（显示当前回合、胜负结果）
    protected JLabel statusLabel;
    // 重新开始游戏按钮
    protected JButton restartButton;
    //悔棋按钮
    protected JButton regretButton;
    //菜单
    protected JMenuBar menuBar;
    // UI 常量
    private static final int GRID_SIZE = 30; // 棋盘格子大小 (像素)
    private static final int MARGIN = 20;    // 棋盘边距

    //初始窗口准备
    public GomokuGUI()
    {
        game = new GomokuGame();
        setTitle("五子棋");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 初始化组件
        statusLabel = new JLabel("游戏开始！当前回合: 黑方", JLabel.CENTER);
        statusLabel.setFont(new Font("宋体", Font.BOLD, 16));
        restartButton = new JButton("重新开始");
        restartButton.addActionListener(e -> restartGame());
        menuBar = new JMenuBar();
        JMenu settingsMenu = new JMenu("游戏设置");
        JMenuItem modeItem = new JMenuItem("切换AI/双人");
        modeItem.addActionListener(e -> game.Changmode());
        JMenuItem colorItem = new JMenuItem("切换AI颜色");
        colorItem.addActionListener(e -> game.Changcolor());
        JMenuItem recodeItem = new JMenuItem("历史记录");
        recodeItem.addActionListener((ActionEvent e) -> {
            // 1. 获取存档的总数
            int archiveCount = game.getStepHistorys().size();
            if (archiveCount == 0)
            {
                JOptionPane.showMessageDialog(this, "目前还没有完成的对局存档！");
                return;
            }
            // 2. 让玩家输入或选择想回放第几局
            int a = archiveCount - 1;
            String input = JOptionPane.showInputDialog(this, "请输入要回放的局数 (1-" + a + "):");
            try
            {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= archiveCount)
                {
                    performReplay(choice - 1); // 调用回放方法
                }
                else
                {
                    JOptionPane.showMessageDialog(this, "无效的局数！");
                }
            }
            catch (NumberFormatException ex)
            {
                // 用户取消或输入非法字符
                JOptionPane.showMessageDialog(this,"请输入合法字符");
            }
        });
        JMenuItem aiItem = new JMenuItem("切换 AI 模式");
        aiItem.addActionListener(e -> {
            if (game.aiMode == AIMode.MINIMAX)
                game.aiMode = AIMode.LLM;
            else
                game.aiMode = AIMode.MINIMAX;
            JOptionPane.showMessageDialog(this, "当前 AI 模式: " + game.aiMode);
        });
        settingsMenu.add(aiItem);
        settingsMenu.add(modeItem);
        settingsMenu.add(colorItem);
        settingsMenu.add(recodeItem);
        menuBar.add(settingsMenu);
        setJMenuBar(menuBar); // 设置窗口菜单
        regretButton = new JButton("悔棋");
        regretButton.addActionListener((ActionEvent e) -> {
            game.regret();
            boardPanel.repaint();
        });
        boardPanel = new BoardPanel();
        // 布局设置
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(statusLabel);
        controlPanel.add(restartButton);
        controlPanel.add(regretButton);
        controlPanel.add(menuBar);
        setLayout(new BorderLayout());
        add(boardPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        // 计算窗口大小并显示
        int boardWidth = game.getBoard().getSize() * GRID_SIZE + 2 * MARGIN;
        int boardHeight = game.getBoard().getSize() * GRID_SIZE + 2 * MARGIN;
        setPreferredSize(new Dimension(boardWidth-10, boardHeight + 60)); // 60 是给控制面板留的高度
        pack();
        setLocationRelativeTo(null); // 居中显示
        setVisible(true);
    }

    //更新棋盘窗口
    private void updateStatus()
    {
        if (game.isGameOver())
        {
            int winner = game.getWinPlayer();
            if (winner == GomokuGame.EMPTY)
            {
                statusLabel.setText("游戏结束！平局。");
            }
            else
            {
                statusLabel.setText("游戏结束！" + game.getPlayerName(winner) + " 获胜！");
            }
        }
        else
        {
            statusLabel.setText("当前回合: " + game.getPlayerName(game.getCurrentPlayer()));
        }
        boardPanel.repaint();
        if(game.Gamemode == 0)
        {
            if (!game.isGameOver() && game.getCurrentPlayer() == game.ai.aiColor)
            {
                performAIMove();
            }
        }
    }
    private void restartGame()
    {
        game.startGame();
        updateStatus();
        allowclik = true;
        setButtonsEnabled(true);
    }
    // 内部类：绘制棋盘和棋子
    private class BoardPanel extends JPanel
    {
        public BoardPanel()
        {
            int size = game.getBoard().getSize();
            // 设置面板大小
            setPreferredSize(new Dimension(size * GRID_SIZE + 2 * MARGIN, size * GRID_SIZE + 2 * MARGIN));
            setBackground(new Color(240, 210, 150));
            // 监听鼠标点击事件
            addMouseListener(new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    if(!allowclik)
                    {
                        return;
                    }
                    if (game.isGameOver())
                    {
                        JOptionPane.showMessageDialog(BoardPanel.this, "游戏已结束，请点击 '重新开始'。", "提示", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    // 将鼠标坐标转换为行列坐标a
                    int col = Math.round((float)(e.getX() - MARGIN) / GRID_SIZE);
                    int row = Math.round((float)(e.getY() - MARGIN) / GRID_SIZE);
                    if (row >= 0 && row < size && col >= 0 && col < size)
                    {
                        if (game.placeMove(row, col))
                        {
                            updateStatus();
                        }
                    }
                }
            });
        }
        //画棋盘
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            //g2d是一只画笔
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = game.getBoard().getSize();
            // 1. 绘制网格线
            g2d.setColor(Color.BLACK);
            for (int i = 0; i < size; i++)
            {
                int x = MARGIN + i * GRID_SIZE;
                int y = MARGIN + i * GRID_SIZE;
                // 绘制垂直线 (从 (MARGIN, y) 到 (SIZE*GRID+MARGIN, y))
                g2d.drawLine(x, MARGIN, x, MARGIN + (size - 1) * GRID_SIZE);
                // 绘制水平线
                g2d.drawLine(MARGIN, y, MARGIN + (size - 1) * GRID_SIZE, y);
            }
            // 2. 绘制星位 (通常在 4, 12, 8, 8 等位置)
            drawStarPoints(g2d, size);
            // 3. 绘制棋子
            for (int r = 0; r < size; r++)
            {
                for (int c = 0; c < size; c++)
                {
                    int piece = game.getBoard().getPiece(r, c);
                    if (piece != GomokuGame.EMPTY)
                    {
                        drawPiece(g2d, r, c, piece);
                    }
                }
            }
            // 4. 标记最后落子位置
            if (game.getBoard().getLastRow() != -1)
            {
                drawLastMoveMarker(g2d, game.getBoard().getLastRow(), game.getBoard().getLastCol());
            }
        }
        private void drawStarPoints(Graphics2D g2d, int size)
        {
            int[] stars = {3, 7, 11}; // 15x15棋盘的星位索引 (0-based)
            g2d.setColor(Color.BLACK);
            for (int r : stars)
            {
                for (int c : stars)
                {
                    if (r < size && c < size)
                    {
                        // 实际的坐标是 (MARGIN + c*GRID_SIZE, MARGIN + r*GRID_SIZE)
                        int x = MARGIN + c * GRID_SIZE;
                        int y = MARGIN + r * GRID_SIZE;
                        g2d.fillOval(x - 3, y - 3, 6, 6);
                    }
                }
            }
        }
        private void drawPiece(Graphics2D g2d, int r, int c, int piece)
        {
            int x = MARGIN + c * GRID_SIZE;
            int y = MARGIN + r * GRID_SIZE;
            int diameter = GRID_SIZE - 2;
            if (piece == GomokuGame.BLACK)
            {
                g2d.setColor(Color.BLACK);
            }
            else if (piece == GomokuGame.WHITE)
            {
                g2d.setColor(Color.WHITE);
            }
            // 绘制棋子，将坐标调整到棋子中心
            g2d.fillOval(x - diameter / 2, y - diameter / 2, diameter, diameter);
            // 增加白色棋子的轮廓
            if (piece == GomokuGame.WHITE)
            {
                g2d.setColor(Color.BLACK);
                g2d.drawOval(x - diameter / 2, y - diameter / 2, diameter, diameter);
            }
        }
        private void drawLastMoveMarker(Graphics2D g2d, int r, int c)
        {
            int x = MARGIN + c * GRID_SIZE;
            int y = MARGIN + r * GRID_SIZE;
            int size = 10;
            g2d.setColor(Color.RED);
            // 绘制一个红色小方块标记最新落子
            g2d.drawRect(x - size / 2, y - size / 2, size, size);
        }
    }
    //  performAIMove 方法
    private void performAIMove()
    {
        setButtonsEnabled(false);
        allowclik = false;
        statusLabel.setText("当前回合: " + game.getPlayerName(game.getCurrentPlayer()) + " 正在思考...");
        boardPanel.repaint(); // 更新提示信息
        // 使用 SwingWorker 在后台线程执行 Minimax 搜索
        SwingWorker<int[], Void> worker =  new SwingWorker<>()
        {
            protected int[] doInBackground() throws Exception
            {
                return game.findAIMove();
            }
            protected void done()
            {
                setButtonsEnabled(true);
                allowclik = true;
                try
                {
                    int[] move = get();
                    // 后续落子和 updateStatus逻辑保持不变
                    if (move != null && move[0] != -1 && move[1] != -1)
                    {
                        game.placeMove(move[0], move[1]);
                        updateStatus();
                    }
                    else
                    {
                        updateStatus();
                    }
                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(GomokuGUI.this, "AI 计算出错: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    private void performReplay(int count)
    {
        // 1.获取所有的历史记录
        LinkedList<GomokuGame.Step> steps = game.getstepHistory(count);
        if (steps.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "还没有下棋记录！");
            return;
        }
        // 2.准备回放环境
        game.clearBoardForReplay(); // 擦干净棋盘
        boardPanel.repaint();
        // 禁用所有按钮
        setButtonsEnabled(false);
        allowclik = false;
        // 3使用 Swing Timer 模拟动态回放
        // 间隔 600 毫秒放下一颗子
        Timer timer = new Timer(600, null);
        timer.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (!steps.isEmpty())
                {
                    // 队列操作,从头部取出一手
                    GomokuGame.Step s = steps.removeFirst();
                    // 模拟落子
                    game.getBoard().placePiece(s.x, s.y, s.color);
                    statusLabel.setText("回放中: " + game.getPlayerName(s.color) + " 落子");
                    boardPanel.repaint();
                }
                else
                {
                    // 播完了
                    timer.stop();
                    setButtonsEnabled(true); // 恢复按钮
                    statusLabel.setText("回放结束！");
                    JOptionPane.showMessageDialog(GomokuGUI.this, "回放完毕");
                }
            }
        });
        timer.start(); // 启动定时器
    }
    // 辅助方法：统一开启/禁用按钮
    private void setButtonsEnabled(boolean enabled)
    {
        regretButton.setEnabled(enabled);
        menuBar.setEnabled(enabled);
        for (int i = 0; i < menuBar.getMenuCount(); i++)
        {
            menuBar.getMenu(i).setEnabled(enabled);
        }
    }
    public static void main(String[] args)
    {
        // 确保在 AWT 事件调度线程中运行 GUI
        SwingUtilities.invokeLater(() -> new GomokuGUI());
    }
}