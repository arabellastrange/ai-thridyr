package pacman.controllers.examples;

import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Game;
import java.util.PriorityQueue;

public class UniformCost extends Controller<Constants.MOVE> {
    private Constants.MOVE[] allMoves= Constants.MOVE.values();
    private PriorityQueue<Constants.MOVE> priorityQueue;

    @Override
    public Constants.MOVE getMove(Game game, long timeDue) {
        priorityQueue = new PriorityQueue<>();
        return null;
    }
}


