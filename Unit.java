package Maloneplayer;
import battlecode.common.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Unit extends Robot {
    Team enemy = rc.getTeam().opponent();
    static Direction exploreDir;
    static final List<Integer> protectionZone = new ArrayList<Integer>() {{
        add(8131);
        add(8639);
        add(8127);
        add(8643);
        add(7873);
        add(8897);
        add(8389);
        add(8381);

    }};

    static MapLocation protectionLoc;


    public Unit(RobotController r) {
        super(r);
        //nav = new Navigation(rc);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();

        int sensorRadius = rc.getType().sensorRadiusSquared;



        //Retrieve flags
        checkForFlag();

        //Confirm home center
        if(homeCenter==null) {
            if(debug) System.out.println("Looking for home center");
            RobotInfo[] nearbyFriendlyRobots = rc.senseNearbyRobots(sensorRadius, rc.getTeam());
            for (int i = nearbyFriendlyRobots.length; --i >= 0; ) {
                RobotInfo info = nearbyFriendlyRobots[i];
                if (info.type == RobotType.ENLIGHTENMENT_CENTER) {
                    homeCenter = info.location;
                    homeCenterID = info.ID;
                }
            }
        }else{
        //reset flag
        rc.setFlag(1);
             }

        //Announce discovery of new enemy center
        if(enemyCenterNearby && enemyCenter ==null && homeCenter!=null){
            if(debug) System.out.println("I discovered a new enemy center!");
            enemyCenter = getHuntLoc(RobotType.ENLIGHTENMENT_CENTER, enemy);
            enemyCenterFlagged = mapLoctoFlagInt(homeCenter, enemyCenter);
        }

        //Announce discovery of new neutral center
        if(neutralCenterNearby && neutralCenter==null && homeCenter!=null){
            if(debug) System.out.println("I discovered a new neutral center!");
            neutralCenter = getHuntLoc(RobotType.ENLIGHTENMENT_CENTER, Team.NEUTRAL);
            neutralCenterFlagged = mapLoctoFlagInt(homeCenter, neutralCenter);
        }

        /*

        //Announce the conversion of the enemy center
        if(enemyCenter!=null){
            if(rc.canSenseLocation(enemyCenter) && !enemyCenterNearby){
                flagToBroadcast = 6; //reset enemycenter loc
            }
        }
         */

        //Announce the conversion of the neutral center
        if(neutralCenter!=null){
            if(rc.canSenseLocation(neutralCenter) && !neutralCenterNearby){
                if(debug) System.out.println("The neutral center is no longer here");
                neutralCenter = null;
                flagToBroadcast = 7; //reset neutralcenter loc
            }
        }

        //Broadcast flags

        if(flagToBroadcast < 10){ //
            rc.setFlag(flagToBroadcast);
            flagToBroadcast=1; //reset
        }

        if(enemyCenterFlagged != 0) {//Broadcast enemies
            if (turnCount % 2 == 0) {
                if (rc.canSetFlag(enemyCenterFlagged)) {
                    rc.setFlag(enemyCenterFlagged);
                    if (debug) System.out.println("Flag set to:" + enemyCenterFlagged + " on turn: "+turnCount) ;
                    enemyCenterFlagged = 0;
                }
            }
        }

        if(neutralCenterFlagged!=0) { //Broadcast neutrals
            if (turnCount % 2 != 0) {
                if (rc.canSetFlag(neutralCenterFlagged)) {
                    rc.setFlag(neutralCenterFlagged);
                    if (debug) System.out.println("Flag set to:" + neutralCenterFlagged+ " on turn: "+turnCount);
                    neutralCenterFlagged = 0;
                }
            }
        }




        logRobotInfo();
    }

    void runPolitian() throws GameActionException{
        actionPerformed = "No Action";

        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        RobotInfo[] attackableNeutral = rc.senseNearbyRobots(actionRadius, Team.NEUTRAL);
        if ((attackable.length != 0 | attackableNeutral.length!=0 )&& rc.canEmpower(actionRadius) && (!friendlyCenterNearby | (enemyMuckrakersNearby&&friendlySlanderNearby))) {
            if(debug) System.out.println("empowering...");
            rc.empower(actionRadius);
            if(debug) System.out.println("empowered");
            return;
        }




        lookForEnemies(); //updates if any units of interest are nearby

        if(neutralCenterNearby){
            targetLoc=getHuntLoc(RobotType.ENLIGHTENMENT_CENTER, Team.NEUTRAL); //Move towards neutral centers
            actionPerformed = "Moving toward neutral Center";
        }else if(enemyMuckrakersNearby){
            targetLoc=getHuntLoc(RobotType.MUCKRAKER, enemy); //Move towards prey
            actionPerformed = "Protecting friendly slanderer";
        }else if(neutralCenter!=null){
            targetLoc=neutralCenter;
            actionPerformed = "Moving toward neutral Center";
        }else if(enemyCenter !=null && turnCount<500){
            targetLoc= enemyCenter;
            actionPerformed = "Moving toward enemy Center";
        }else if(turnCount>=500){
            protectionLoc=null;
            if(homeCenter!=null) {
            for (int i = 0; i < protectionZone.size(); i++) { //Find a position in the circle
                protectionLoc = flagInttoMapLoc(homeCenter, protectionZone.get(i));
                if (rc.canSenseLocation(protectionLoc)) {
                    if (rc.onTheMap(protectionLoc)) {
                        if (!rc.isLocationOccupied(protectionLoc)) {
                            targetLoc = protectionLoc;
                            actionPerformed = "Forming a defensive circle";
                            break;
                        }
                    }
                }
            }


                if (protectionZone.contains(mapLoctoFlagInt(homeCenter, rc.getLocation()))) {//Stay put if in circle
                    targetLoc = rc.getLocation();
                }
            }
            if(protectionLoc==null){ //Go home if not near defense circle
                targetLoc = homeCenter;
                actionPerformed = "Protecting Home Center";
            }
        }else{
            targetLoc=explore();
            actionPerformed = "Exploring";
        }

        //Move
        if(targetLoc != null) {
            goTo(targetLoc);
        }else{
            actionPerformed = "Moved randomly";
            goTo(explore());
        }
    }


    MapLocation getHuntLoc(RobotType prey, Team team) throws GameActionException {
        RobotInfo[] nearbyEnemyRobots = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, team);
        for (int i = 0; i <nearbyEnemyRobots.length;i++ ) {
            RobotInfo info = nearbyEnemyRobots[i];
            if (info.type == prey && info.team == team) {
                rc.setIndicatorLine(rc.getLocation(),info.location,255,0,0);
                return info.location;
            }
        }
        return null;
    }


    MapLocation explore() throws GameActionException{
        if(exploreDir == null) { //Create new random location if not avalible
            exploreDir = randomDirection();
        }
        if(rc.isReady() && !rc.onTheMap(rc.getLocation().add(exploreDir))){ //Reset location if blocked
            exploreDir=randomDirection();
        }

        return rc.getLocation().add(exploreDir);
    }

    void checkForFlag() throws GameActionException{
        //Retrieve flags
        if (homeCenterID > 0 && homeCenter!=null) {
            if (rc.canGetFlag(homeCenterID)) {
                int seenFlag = rc.getFlag(homeCenterID);
                if (seenFlag > 0) {
                    if (seenFlag == 6) { //reset enemy center
                        enemyCenter=null;
                    }else if (seenFlag ==7){//reset neutral center
                        neutralCenter = null;
                    }else if (enemyCenter == null && turnCount%2 ==0){
                        if(debug) System.out.println("Checking flags for enemy center: "+turnCount);
                        enemyCenter = flagInttoMapLoc(homeCenter, seenFlag);
                    }else if (neutralCenter==null && turnCount%2 !=0){
                        if(debug) System.out.println("Checking flags for neutral center: "+turnCount);
                        neutralCenter = flagInttoMapLoc(homeCenter, seenFlag);
                    }
                }
            }
        }
    }
}
