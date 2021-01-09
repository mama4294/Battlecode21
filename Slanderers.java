package Maloneplayer;
import battlecode.common.*;

public class Slanderers extends Unit{

    public Slanderers(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        actionPerformed = "No Action";

        if (rc.getType() == RobotType.SLANDERER) {
            lookForEnemies(); //updates if any units of interest are nearby

            if(enemyMuckrakersNearby){
                actionPerformed = "Running away from muckrakers";
                tryMove(rc.getLocation().directionTo(getHuntLoc(RobotType.MUCKRAKER,enemy)).opposite());
            }

            else if(homeCenter!=null){
                actionPerformed = "Staying Home";
                goTo(homeCenter);
            }else {
                tryMove(randomDirection());
                actionPerformed = "Moved randomly";
            }
        }else if(rc.getType() == RobotType.POLITICIAN){
            runPolitian();
        }
    }
}
