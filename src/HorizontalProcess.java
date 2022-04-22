import com.oocourse.elevator3.PersonRequest;

import java.util.ArrayList;

public class HorizontalProcess implements Process {
    private final RequestQueue requestQueue;
    //是否已有电梯在这曾开门
    private final boolean[] hasOpen;

    public HorizontalProcess(RequestQueue requestQueue, int floor) {
        this.requestQueue = requestQueue;
        //this.floor = floor;
        hasOpen = new boolean[5];
    }

    @Override
    public void run(Elevator elevator) {
        char fromBuilding;
        char toBuilding;
        while (true) {
            if (requestQueue.isEnd() && requestQueue.isEmpty()) {
                return;
            }
            //目前最先放进去的
            PersonRequest request = requestQueue.findOneRequest();
            if (request == null) {
                continue;
            }
            fromBuilding = request.getFromBuilding();
            toBuilding = request.getToBuilding();
            //先去接人
            if (elevator.getBuildingId() == fromBuilding) {
                judgeOpen(true, elevator.getBuildingId(), elevator);
                if (direction(fromBuilding, toBuilding)) {
                    //if is true,turn right
                    goRight(elevator);
                } else {
                    //if is false,turn left
                    goLeft(elevator);
                }
            } else if (direction(elevator.getBuildingId(), fromBuilding)) {
                goRight(elevator);
            } else if (!direction(elevator.getBuildingId(), fromBuilding)) {
                goLeft(elevator);
            }
        }
    }

    public boolean direction(char fromBuilding, char toBuilding) {
        char nextBuilding;
        nextBuilding = (char) (fromBuilding + 1);
        nextBuilding = nextBuilding > 'E' ? 'A' : nextBuilding;
        if (nextBuilding == toBuilding) {
            return true;
        }
        nextBuilding++;
        nextBuilding = nextBuilding > 'E' ? 'A' : nextBuilding;
        return nextBuilding == toBuilding;
    }

    public void goRight(Elevator elevator) {
        boolean willOpen;
        while (elevator.getPeopleNum() != 0 || !requestQueue.isEmpty()) {
            willOpen = elevator.rightElevator();
            judgeOpen(willOpen, elevator.getBuildingId(), elevator);
        }

    }

    public void goLeft(Elevator elevator) {
        boolean willOpen;
        while (elevator.getPeopleNum() != 0 || !requestQueue.isEmpty()) {
            willOpen = elevator.leftElevator();
            judgeOpen(willOpen, elevator.getBuildingId(), elevator);
        }
    }

    public boolean judgeOpen(boolean willOpen, char nowBuilding, Elevator elevator) {
        if (!canOpen(elevator,nowBuilding)) {
            //不该这层楼停
            return false;
        }
        boolean wantIn = false;
        boolean open = false;
        synchronized (requestQueue) {
            for (PersonRequest entry : requestQueue.getPersonRequests()) {
                if (entry.getFromBuilding() == nowBuilding &&
                        canOpen(elevator, entry.getToBuilding())) {
                    wantIn = true;
                    break;
                }
            }
        }
        //若不超载且有人在，或者里面的人要下
        if ((elevator.getPeopleNum() < elevator.getMaxLoad() && wantIn) || willOpen) {
            //sleep for 400ms
            if (!hasOpen[nowBuilding - 'A'] || willOpen) {
                hasOpen[nowBuilding - 'A'] = true;
                elevator.openElevator();
                open = true;
                synchronized (requestQueue) {
                    ArrayList<PersonRequest> newPersonRequest =
                            new ArrayList<>(requestQueue.getPersonRequests());
                    for (PersonRequest entry : requestQueue.getPersonRequests()) {
                        if (entry.getFromBuilding() == nowBuilding &&
                                canOpen(elevator,entry.getToBuilding()) &&
                                elevator.getPeopleNum() < elevator.getMaxLoad()) {
                            elevator.addPersonRequest(entry);
                            newPersonRequest.remove(entry);
                        }
                    }
                    requestQueue.setPersonRequests(newPersonRequest);
                }
                elevator.closeElevator();
                hasOpen[nowBuilding - 'A'] = false;
            }
        }
        return open;
    }

    public boolean canOpen(Elevator elevator,char nowBuilding) {
        return ((elevator.getSwitchInfo() >> (nowBuilding - 'A')) & 1) == 1;
    }
}
