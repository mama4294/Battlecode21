package Maloneplayer;
import battlecode.common.*;


public class Politicians extends Unit {
    public Politicians(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        runPolitian();
    }
}
