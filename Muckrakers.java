package Maloneplayer;
import battlecode.common.*;

public class Muckrakers extends Unit {

    public Muckrakers(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();


        actionPerformed = "No Action";

        //Check for slanderers to kill
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    actionPerformed = "Exposed a Slanderer";
                    rc.expose(robot.location);
                    rc.setIndicatorLine(rc.getLocation(),robot.location, 252, 248, 3);
                    return;
                }
            }
        }



        //Determine where to gso
        lookForEnemies();
        if(enemySlandererNearby){
            targetLoc=getHuntLoc(RobotType.SLANDERER, enemy); //Move towards Slanderers
            actionPerformed = "Moving toward enemy Slanderer";
        }else if(enemyCenter !=null){
            targetLoc= enemyCenter;
            actionPerformed = "Moving toward enemy Center";
        } else{
            targetLoc=explore();
            actionPerformed = "Exploring";
        }

        //Move
        if(targetLoc != null) {
            goTo(targetLoc);
        }else{
            actionPerformed = "Moved randomly";
            tryMove(randomDirection());
        }



    }
}
