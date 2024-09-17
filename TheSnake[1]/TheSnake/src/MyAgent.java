import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import za.ac.wits.snake.DevelopmentAgent;

public class MyAgent extends DevelopmentAgent {

    private int appleX;
    private int appleY;
    private List<List<int[]>> otherSnakePositions; // Store positions of other snakes
    private int mySnakeNum; // Store your snake's index
    private double appleValue; //curr value of apple

    public static void main(String args[]) {
        MyAgent agent = new MyAgent();
        MyAgent.start(agent, args);
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String initString = br.readLine();
            String[] temp = initString.split(" ");
            int nSnakes = Integer.parseInt(temp[0]);
            int numObstacles = 3; // Number of obstacles, change as needed
            otherSnakePositions = new ArrayList<>(); // Initialize list for other snake positions
            appleValue = 5.0;

            while (true) {
                String line = br.readLine();
                if (line.contains("Game Over")) {
                    break;
                }

                int[][] board = new int[50][50];

                String[] appleCoords = line.split(" ");
                appleX = Integer.parseInt(appleCoords[0]);
                appleY = Integer.parseInt(appleCoords[1]);
                Coordinates apple = new Coordinates(appleX, appleY);

                for (int i = 0; i < 50; i++) {
                    for (int j = 0; j < 50; j++) {
                        board[i][j] = 2;

                    }
                }

                // Read and process obstacle positions from the game input
                for (int i = 0; i < numObstacles; i++) {
                    String obstacles = br.readLine();
                    drawSnakes(obstacles, 8, board);
                }

                mySnakeNum = Integer.parseInt(br.readLine());
                Coordinates tail = apple;
                Coordinates myCoords = new Coordinates(0, 0);
                for (int i = 0; i < nSnakes; i++) {
                    String snakeLine = br.readLine();
                    String[] snakeParts = snakeLine.split(" ", 4);

                    if (snakeParts[0].equals("alive") && i != mySnakeNum) {
                        String coords = snakeParts[3];
                        int snakeLength = Integer.parseInt(snakeParts[1]);
                        int kills = Integer.parseInt(snakeParts[2]);

                        String[] part = coords.split(" ");
                        String[] snakeHead = snakeParts[0].split(",");
                        String[] snakeTail = snakeParts[snakeParts.length - 1].split(",");

                        drawSnakes(coords, 8, board);
                    }
                    if (i == mySnakeNum) {
                        String myPos = snakeParts[3];
                        String[] myP = myPos.split(" ");
                        String[] myHead = myP[0].split(",");
                        String[] myTail = myP[myP.length - 1].split(",");

                        myCoords = new Coordinates(Integer.parseInt(myHead[0]), Integer.parseInt(myHead[1]));
                        tail = new Coordinates(Integer.parseInt(myTail[0]), Integer.parseInt(myTail[1]));

                        if (snakeParts[0].equals("alive")) {
                            drawSnakes(myPos, 8, board);
                        }
                    }
                }
                
                appleValue = appleValue - 0.1;
                
                runAStar toApple = new runAStar(board, myCoords, apple);
                int move = toApple.aStar();

                if (move == 5) {
                    runAStar toTail = new runAStar(board, myCoords, tail);
                    int tailMove = toTail.aStar();
                    if (tailMove != 5) {
                        move = tailMove;
                    } else {
                        Random random = new Random();
                        move = random.nextInt(5);

                        if (move == 5) {
                            move = random.nextInt(5);
                        }
                    }
                }
                System.out.println(move);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void drawLine(int[][] board, String position1, String position2, int j) {
        String[] coordinate1 = position1.split(",");
        String[] coordinate2 = position2.split(",");

        if (coordinate1[0].equals(coordinate2[0])) {
            int x = Integer.valueOf(coordinate1[0]);
            int y1 = Integer.valueOf(coordinate1[1]);
            int y2 = Integer.valueOf(coordinate2[1]);

            int distance = Math.abs(y1 - y2);

            if (y1 < y2) {
                for (int i = 0; i <= distance; i++) {
                    board[x][y1 + i] = j;
                }

            } else {
                for (int i = 0; i <= distance; i++) {
                    board[x][y2 + i] = j;
                }
            }
        } else {
            int y = Integer.valueOf(coordinate1[1]);
            int x1 = Integer.valueOf(coordinate1[0]);
            int x2 = Integer.valueOf(coordinate2[0]);

            int distance = Math.abs(x1 - x2);

            if (x1 < x2) {
                for (int i = 0; i <= distance; i++) {
                    board[x1 + i][y] = j;
                }
            } else {
                for (int i = 0; i <= distance; i++) {
                    board[x2 + i][y] = j;
                }
            }
        }
    }

    public static void drawSnakes(String myCoords, int j, int[][] board) {
        String[] pos = myCoords.split(" ");
        int length = pos.length - 1;

        for (int i = 0; i < length; i++) {
            drawLine(board, pos[i], pos[i + 1], j);
        }

        String[] coord1 = pos[0].split(",");
        board[Integer.valueOf(coord1[0])][Integer.valueOf(coord1[1])] = j;
    }

    

    class runAStar {
        int[][] board;
        Coordinates myHead;
        Coordinates appleCoord;
        boolean[][] visited;

        public runAStar(int[][] board, Coordinates myHead, Coordinates appleCoord) {
            this.myHead = myHead;
            this.board = board;
            this.appleCoord = appleCoord;
            visited = new boolean[50][50];
        }

        public int aStar() {
            int direction = 5; // Initially, move straight

            // Array of possible moves: Up, Down, Left, Right
            int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
            
            for (int i = 0; i < 50; i++) {
            	for(int j = 0; j < 50; j++) {
            		visited[i][j] = false;  //reset visited array
            	}
            }

            // Create a priority queue for A* where the elements are sorted by their total cost
            PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
            priorityQueue.add(new Node(myHead, 0, heuristic(myHead, appleCoord,appleValue), null));

            // Initialize a visited array to keep track of visited positions
            boolean[][] visited = new boolean[50][50];
            visited[myHead.x][myHead.y] = true;

            while (!priorityQueue.isEmpty()) {
                Node currentNode = priorityQueue.poll();
                Coordinates currentPos = currentNode.position;

                if (currentPos.equals(appleCoord)) {
                    // Found the apple, reconstruct the newRoute from the apple to the head
                    ArrayList<Coordinates> newRoute = new ArrayList<>();
                    Node backtrace = currentNode;

                    while (backtrace != null) {
                        newRoute.add(backtrace.position);
                        backtrace = backtrace.parent;
                    }

                    if (newRoute.size() >= 2) {
                        Coordinates start = newRoute.get(newRoute.size() - 2);
                        Coordinates end = newRoute.get(newRoute.size() - 1);

                        if (start.x == end.x && start.y == end.y - 1) {
                            direction = 0; // Move up
                        } else if (start.x == end.x && start.y == end.y + 1) {
                            direction = 1; // Move down
                        } else if (start.x == end.x - 1 && start.y == end.y) {
                            direction = 2; // Move left
                        } else if (start.x == end.x + 1 && start.y == end.y) {
                            direction = 3; // Move right
                        }

                        return direction;
                    }
                }

                for (int[] move : directions) {
                    int newX = currentPos.x + move[0];
                    int newY = currentPos.y + move[1];

                    if (newX >= 0 && newX < 50 && newY >= 0 && newY < 50 && board[newX][newY] != 8 && !visited[newX][newY]) {
                        Coordinates neighbour = new Coordinates(newX, newY);
                        int newGCost = currentNode.gCost + 1;
                        int newHCost = heuristic(neighbour, appleCoord,appleValue);
                        int newFCost = newGCost + newHCost;

                        // Check if a shorter newRoute to the same position is in the priority queue
                        boolean hasShorternewRoute = false;
                        for (Node nodeInQueue : priorityQueue) {
                            if (nodeInQueue.position.equals(neighbour) && nodeInQueue.gCost > newGCost) {
                                hasShorternewRoute = true;
                                break;
                            }
                        }

                        if (!hasShorternewRoute) {
                            // Check if this move provides a shortcut to the apple
                            if (newHCost < heuristic(myHead, appleCoord, appleValue)) {
                                // Prioritize shortcuts
                                newHCost = 0;
                            }

                            priorityQueue.add(new Node(neighbour, newGCost, newHCost, currentNode));
                            visited[newX][newY] = true;
                        }
                    }
                }
            }
            return direction;
        }

        //(Manhattan distance)
        public int heuristic(Coordinates a, Coordinates b, double appleValue) {
        	int baseC = (int) Math.ceil(appleValue);
            return Math.abs(a.x - b.x) + Math.abs(a.y - b.y) + baseC;
        }

        // Node class for A* search
        class Node implements Comparable<Node> {
            Coordinates position;
            int gCost;
            int hCost;
            Node parent;

            public Node(Coordinates position, int gCost, int hCost, Node parent) {
                this.position = position;
                this.gCost = gCost;
                this.hCost = hCost;
                this.parent = parent;
            }

            @Override
            public int compareTo(Node other) {
                int fCost = gCost + hCost;
                int otherFCost = other.gCost + other.hCost;
                return Integer.compare(fCost, otherFCost);
            }
        }
    }



}
