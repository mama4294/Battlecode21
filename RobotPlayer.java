package Maloneplayer;
import battlecode.common.*;


public strictfp class RobotPlayer {
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    public static void run(RobotController rc) throws GameActionException {
        Robot me = null;

        switch (rc.getType()) {
            case ENLIGHTENMENT_CENTER:
                me = new EnlightenmentCenter(rc);
                break;
            case POLITICIAN:
                me = new Politicians(rc);
                break;
            case SLANDERER:
                me = new Slanderers(rc);
                break;
            case MUCKRAKER:
                me = new Muckrakers(rc);
                break;
        }

        while (true) {
            try {
                me.takeTurn();
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();
            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception"); // darn
                e.printStackTrace();
            }
        }
    }
}
