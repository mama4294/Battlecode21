package Maloneplayer;
import battlecode.common.*;

import java.util.LinkedList;

public class EnlightenmentCenter extends Robot{


    public EnlightenmentCenter(RobotController r) {
        super(r);
    }

    int numPolitician = 0;
    int numSlanderer = 0;
    int numMuckrakers = 0;
    int constructionCost = 0;
    int proposedBid;
    RobotType toBuild;
    LinkedList<Integer> friendlyIDs = new LinkedList<Integer>();
    LinkedList<Integer> friendlyRobotAge = new LinkedList<Integer>();
    LinkedList<RobotType> friendlyRobotTypes = new LinkedList<RobotType>();



    public void takeTurn() throws GameActionException {
        super.takeTurn();

        //Reset cencus
        numPolitician = 0;
        numSlanderer = 0;
        numMuckrakers = 0;
        actionPerformed = "No Action";
        toBuild = null;

        //Get new robot ID's
            RobotInfo[] nearbyFriendlyRobots = rc.senseNearbyRobots(3, rc.getTeam());
            for (int i = nearbyFriendlyRobots.length; --i >= 0; ) {

                RobotInfo info = nearbyFriendlyRobots[i];
                if (info.type != RobotType.ENLIGHTENMENT_CENTER && rc.getFlag(info.getID())==0){
                    if (!friendlyIDs.contains(info.ID)) {
                        if(debug) System.out.println("Added a new child: " + info.getType() + info.getID());
                        friendlyIDs.add(info.ID);
                        friendlyRobotTypes.add(info.type);
                        friendlyRobotAge.add(0);
                    }
                }
            }


        lookForEnemies(); //Check for nearby units of interest


        if (homeCenter==null){
            homeCenter=rc.getLocation();
        }

        //Get Cencus
        for (int i = 0; i < friendlyIDs.size(); i++) { //Remove dead units ID's
            if (!rc.canGetFlag(friendlyIDs.get(i))) {
                friendlyIDs.remove(i);
                friendlyRobotTypes.remove(i);
                friendlyRobotAge.remove(i);
            } else {  //Find alive units
                //Cencus
                RobotType robotToCount = friendlyRobotTypes.get(i);
                if(robotToCount==RobotType.POLITICIAN){
                    numPolitician++;
                }else if(robotToCount==RobotType.SLANDERER){
                    numSlanderer++;
                    int currentAge = friendlyRobotAge.get(i);
                    friendlyRobotAge.set(i,currentAge+1);

                    if(currentAge+1 >= 300){
                        friendlyRobotTypes.set(i,RobotType.POLITICIAN); //Convert old slanderer to polician
                        if(debug) System.out.println("Slanderer " + friendlyIDs.get(i) + "grew into a Politician");
                    }

                }else if(robotToCount==RobotType.MUCKRAKER){
                    numMuckrakers++;
                }

                //Check for enemy flag on unit
                int seenFlag = rc.getFlag(friendlyIDs.get(i));

                if(enemyCenter ==null && (turnCount-1)%2 ==0) {
                    if (seenFlag > 10) { //flag is not default
                        enemyCenterInt = seenFlag;
                        enemyCenter = flagInttoMapLoc(homeCenter, seenFlag);
                        if(debug) System.out.println("Received new intel about an enemy center at: " + enemyCenter + " | Turn count; "+ turnCount);

                    }
                }
                //Check for neutral flag on unit
                if(neutralCenter==null && (turnCount-1)%2 !=0) {
                    if (seenFlag > 10) { //flag is not default
                        neutralCenterInt = seenFlag;
                        neutralCenter = flagInttoMapLoc(homeCenter, seenFlag);
                        if(debug) System.out.println("Received new intel about a neutral center at: " + neutralCenter+ " | Turn count; "+ turnCount);
                    }
                }
                if(seenFlag ==6){
                    if(debug) System.out.println("Resetting enemy center location");
                    enemyCenter=null; //reset enemy location
                    rc.setFlag(6);
                }else if(seenFlag ==7){
                    if(debug) System.out.println("Resetting neutral center location");
                    neutralCenter=null; //reset neutral location
                    rc.setFlag(7);
                }
            }
        }

        //Raise flag
        if(turnCount%2 ==0) {
            if (enemyCenter != null) {
                    flagToBroadcast=enemyCenterInt;
            }else{
                flagToBroadcast=0;
            }
        }else if (turnCount%2 !=0){
            if (neutralCenter != null) {
                flagToBroadcast=neutralCenterInt;
            }else{
                flagToBroadcast=0;
            }
        }

        //Broadcast flag
        if(rc.canSetFlag(flagToBroadcast)) {
            rc.setFlag(flagToBroadcast);
        }


        //Determine what to build based on cencus
        constructionCost = 50;
        if(rc.getInfluence()>100) {
            if (enemyMuckrakersNearby){
                toBuild = RobotType.POLITICIAN;
            }else if(turnCount<3){
                toBuild = RobotType.SLANDERER;
                constructionCost = 110;
            } else if(numMuckrakers < 3) {
                toBuild = RobotType.MUCKRAKER;
                constructionCost = 1;
            } else if (numSlanderer < 3) {
                if(roundSinceLastMuckraker>50){
                    constructionCost=rc.getInfluence()/3;
                    if(constructionCost>950) constructionCost=950;
                }
                toBuild = RobotType.SLANDERER;
            } else if (numPolitician < 3 | (turnCount>500 && numPolitician < 8)) {
                toBuild = RobotType.POLITICIAN;
            } else {
                toBuild = null;
            }
        }

        //Build unit
        if(toBuild!=null) {
            actionPerformed = "Saving up for a " + toBuild;
            for (Direction dir : directions) {
                if (rc.canBuildRobot(toBuild, dir, constructionCost)) {
                    rc.buildRobot(toBuild, dir, constructionCost);
                    actionPerformed = "Built " + toBuild;
                } else {
                    break;
                }
            }
        }else actionPerformed="Building Defence";

        //Determine Bid
        proposedBid = rc.getInfluence()/(3001-turnCount);

        if(turnCount>500){
            proposedBid++;
        }

        if(1>proposedBid){
            proposedBid=1;
        }

        //Bid
        if (rc.canBid(1)) {
            rc.bid(1); // submit a bid for a vote
        }

        if(!rc.isReady()){
            actionPerformed = "Cooldown " + Math.round(rc.getCooldownTurns() * 100.0) / 100.0;
        }

        //Print Log
        logRobotInfo();
    }


    public void logRobotInfo() {
        if (debug) {
            System.out.println("Type: " + rc.getType() + " | " + actionPerformed +" Turn: "+turnCount+  " - Bytecode used: " + Clock.getBytecodeNum() +
                    " | Bytecode left: " + Clock.getBytecodesLeft() +
                    " | Influence: " + rc.getInfluence() + " | Conviction: " + rc.getConviction() + " | Bid: " + proposedBid +
                    " | Enemy Center: " + enemyCenter + " | Neutral Center " + neutralCenter +
                    " | Empower Factor: " + Math.round(rc.getEmpowerFactor(rc.getTeam(), 0) * 100.0) / 100.0 +
                    " | Enemy Empower Factor: " + Math.round(rc.getEmpowerFactor(rc.getTeam().opponent(), 0) * 100.0) / 100.0+
                    " | Politicans: " + numPolitician + " | Slanderers: " + numSlanderer + " | Muckrakers: " + numMuckrakers);
        }
    }
}
