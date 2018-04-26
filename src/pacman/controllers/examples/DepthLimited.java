package pacman.controllers.examples;

import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.Random;

public class DepthLimited extends Controller<Constants.MOVE> {
    int depth = 36;                                                                                                                                                                                                                                       ; //4
    int iterations = 700; //64
    double best;
    Constants.MOVE winningMove;

    @Override
    public Constants.MOVE getMove(Game game, long timeDue) {
       best = Double.NEGATIVE_INFINITY;
       int current = game.getPacmanCurrentNodeIndex();

       Constants.MOVE[] allMoves = game.getPossibleMoves(current);

       for(Constants.MOVE move : allMoves){
            double score = runSimulation(move, game);
            if (score > best){
                best = score;
                winningMove = move;
            }
       }

       return winningMove;

    }

    private double runSimulation(Constants.MOVE move, Game game) {
        Legacy ghosts = new Legacy();
        Cluster2 strategy = new Cluster2();
        int score = 0;
        int currentDepth;
        for(int i = 0; i < iterations; i++){
            Game copy = game.copy();
            copy.advanceGame(move, ghosts.myMoves);
            currentDepth = 0;
            while (currentDepth < depth && copy.getPossibleMoves(copy.getPacmanCurrentNodeIndex()).length != 0 ){
                Constants.MOVE nextBestMove = strategy.getMove();
                copy.advanceGame(nextBestMove, ghosts.myMoves);
                currentDepth++;
            }
            score += copy.getScore();
            //copy.undo();
        }
        return (score/iterations);
    }
}
