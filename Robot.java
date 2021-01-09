package Maloneplayer;
import battlecode.common.*;

//TODO build actions to determine what happened each turn
//TODO add explore method

public class Robot {

    RobotController rc;
    boolean debug = true;
    int turnCount = 0;
    int mapKey = 128;
    int homeCenterID = 0;
    int enemyCenterFlagged = 0;
    int neutralCenterFlagged = 0;
    int enemyCenterInt = 0;
    int neutralCenterInt = 0;
    int flagToBroadcast = 0;
    int roundSinceLastMuckraker = 0;
    Boolean enemyMuckrakersNearby=false;
    Boolean enemySlandererNearby=false;
    Boolean enemyPoliticanNearby=false;
    Boolean enemyCenterNearby=false;
    Boolean neutralCenterNearby=false;
    Boolean friendlyCenterNearby=false;
    Boolean friendlySlanderNearby=false;
    static MapLocation homeCenter;
    static MapLocation enemyCenter;
    static MapLocation neutralCenter;
    static MapLocation targetLoc;
    String actionPerformed;

    static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    public Robot(RobotController r) {
        this.rc = r;
        //comms = new Communications(rc);
    }

    public void takeTurn() throws GameActionException {
        if(turnCount==0){
            turnCount=rc.getRoundNum();
        }
        turnCount += 1;
    }

    public void logRobotInfo() {
        if (debug) {
            if (rc.isReady()) {
                System.out.println("Type: " + rc.getType() + ": " + rc.getID() + " | " + actionPerformed + " - Bytecode used: " + Clock.getBytecodeNum() +
                        " | Bytecode left: " + Clock.getBytecodesLeft() + " | Flag: " + flagToBroadcast +
                        " | Home Center: " + homeCenter +
                        " | Enemy Center: " + enemyCenter + " | Neutral Center " + neutralCenter +
                        " | Influence: " + rc.getInfluence() + " | Conviction: " + rc.getConviction() +
                        " | Cooldown: " + Math.round(rc.getCooldownTurns() * 100.0) / 100.0);
            } else {
                System.out.println("Cooldown: " + Math.round(rc.getCooldownTurns() * 100.0) / 100.0);
            }
        }
    }

    public Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    public RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    boolean tryMove(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    // tries to move in the general direction of dir
    boolean goTo(Direction dir) throws GameActionException {
        if(dir == Direction.CENTER){
            if(tryMove(dir)) return true;
        }

        Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateLeft().rotateLeft(), dir.rotateRight(), dir.rotateRight().rotateRight()};
        for (Direction d : toTry) {
            if (tryMove(d))
                return true;
        }
        return false;
    }

    // navigate towards a particular location
    boolean goTo(MapLocation destination) throws GameActionException {
        if (rc.getLocation().equals(destination)) {
            return goTo(Direction.CENTER);
        } else {
            return goTo(rc.getLocation().directionTo(destination));
        }
    }

    //set enemy center location
    int mapLoctoFlagInt (MapLocation homeCenter, MapLocation loc)throws GameActionException{
        int x = loc.x-homeCenter.x;
        int y = loc.y-homeCenter.y;
        int mappedLoc = (x+65)*mapKey+(y+65);
        //System.out.println("X offset: " +x+" | Y offset: "+y+" | HashedLoc: "+mappedLoc);
        return mappedLoc;
    }

    MapLocation flagInttoMapLoc (MapLocation homeCenter, int flagInt)throws GameActionException{
        //if(debug) System.out.println("Decipering Flag Int:"+flagInt+ " on turn: "+turnCount);
        int x = (flagInt/mapKey);
        int y = (flagInt%mapKey);
        //System.out.println("X offset: " +x+" | Y offset: "+y);
        MapLocation result = homeCenter.translate(x-65,y-65);
        return(result);
    }


    void lookForEnemies() throws GameActionException{
        enemyMuckrakersNearby= false;
        enemySlandererNearby = false;
        enemyPoliticanNearby = false;
        enemyCenterNearby = false;
        neutralCenterNearby = false;
        friendlyCenterNearby = false;
        friendlySlanderNearby=false;

        RobotInfo[] nearbyEnemyRobots = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared);
        for (int i = 0; i <nearbyEnemyRobots.length;i++ ) {
            RobotInfo info = nearbyEnemyRobots[i];
            if (info.type == RobotType.MUCKRAKER && info.team == rc.getTeam().opponent()) {
                enemyMuckrakersNearby= true;
                roundSinceLastMuckraker=0;
            }else if(info.type == RobotType.SLANDERER && info.team == rc.getTeam().opponent()){
                enemySlandererNearby = true;
            }else if(info.type == RobotType.POLITICIAN && info.team == rc.getTeam().opponent()){
                enemyPoliticanNearby = true;
            }else if(info.type == RobotType.ENLIGHTENMENT_CENTER && info.team == rc.getTeam().opponent()){
                enemyCenterNearby = true;
            }else if(info.type == RobotType.ENLIGHTENMENT_CENTER && info.team == rc.getTeam() && rc.getLocation().distanceSquaredTo(info.location)<3){
                friendlyCenterNearby = true;
            }else if(info.type == RobotType.SLANDERER && info.team == rc.getTeam()){
                friendlySlanderNearby = true;
            }else if(info.team == Team.NEUTRAL){
                neutralCenterNearby = true;
            }
        }
        roundSinceLastMuckraker++;
    }
}
