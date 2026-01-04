// NewGomokuAI.java
// 简单封装：使用新的搜索引擎作为替代 AI。不会修改现有 GomokuAI.java；你可以在游戏中调用该类。

public class NewGomokuAI {
    private final GomokuSearch search;

    public NewGomokuAI() {
        GomokuEvaluator evaluator = new GomokuEvaluator();
        MoveGenerator moveGen = new MoveGenerator(2, 200);
        // 默认：最大深度 6，时间限制 2000ms
        this.search = new GomokuSearch(evaluator, moveGen, 6, 2000);
    }

    // 返回数组 {x, y}；如果找不到返回 {-1, -1}
    public int[] findBestMove(Board board, int player) {
        return search.findBestMove(board, player);
    }
}
