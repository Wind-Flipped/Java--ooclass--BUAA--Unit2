import com.oocourse.elevator3.PersonRequest;

import java.util.ArrayList;

public class VerticalProcess implements Process {
    private final RequestQueue requestQueue;
    //是否已有电梯在这曾开门
    private final boolean[] hasOpen;

    public VerticalProcess(RequestQueue requestQueue, int id) {
        this.requestQueue = requestQueue;
        hasOpen = new boolean[10];
    }

    @Override
    public void run(Elevator elevator) {
        int toFloor;
        int fromFloor;
        while (true) {
            if (requestQueue.isEnd() && requestQueue.isEmpty()) {
                return;
            }
            elevator.clear();
            //目前最先放进去的
            PersonRequest request = requestQueue.findOneRequest();
            if (request == null) {
                continue;
            }
            fromFloor = request.getFromFloor();
            toFloor = request.getToFloor();
            //先去接人
            if (elevator.getFloor() == fromFloor) {
                judgeOpen(false, elevator.getFloor(), fromFloor < toFloor, elevator);
                if (elevator.getPeopleNum() == 0) {
                    //由于竞争没有接到人
                    continue;
                } else {
                    if (fromFloor < toFloor) {
                        goUp(elevator);
                    } else {
                        goDown(elevator);
                    }
                }

            } else if (elevator.getFloor() < fromFloor) {
                goUp(elevator);
            } else if (elevator.getFloor() > fromFloor) {
                goDown(elevator);
            }

            //            elevators[0].openElevator();
            //            elevators[0].addPersonRequest(request, elevators[0].getFloor() < toFloor);
            //            elevators[0].closeElevator();
            //judgeOpen(true, elevators[0].getFloor(), elevators[0].getFloor() < toFloor);
            //接完人后
            //            if (elevators[0].getFloor() < toFloor) {
            //                while (elevators[0].getFloor() < elevators[0].getMaxDestination()) {
            //                    try {
            //                        willOpen = elevators[0].upElevator();
            //                    } catch (InterruptedException e) {
            //                        e.printStackTrace();
            //                    }
            //                    judgeOpen(willOpen, elevators[0].getFloor(), true);
            //                }
            //            } else {
            //                while (elevators[0].getFloor() > elevators[0].getMinDestination()) {
            //                    try {
            //                        willOpen = elevators[0].downElevator();
            //                    } catch (InterruptedException e) {
            //                        e.printStackTrace();
            //                    }
            //                    judgeOpen(willOpen, elevators[0].getFloor(), false);
            //                }
            //            }
            //notifyAll();
        }
    }

    public void goDown(Elevator elevator) {
        boolean willOpen = false;
        while (elevator.getFloor() > elevator.getMinDestination()
                || elevator.getFloor() > requestQueue.getLowestFromFloor()) {
            try {
                willOpen = elevator.downElevator();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (elevator.getFloor() <= elevator.getMinDestination()
                    && elevator.getFloor() <= requestQueue.getLowestFromFloor()) {
                if (ifChangeWay(false, elevator.getFloor(), elevator)) {
                    break;
                }
            } else {
                judgeOpen(willOpen, elevator.getFloor(), false, elevator);
            }
        }
        if (elevator.getPeopleNum() != 0) {
            goUp(elevator);
        }
    }

    public void goUp(Elevator elevator) {
        boolean willOpen = false;
        while (elevator.getFloor() < elevator.getMaxDestination()
                || elevator.getFloor() < requestQueue.getHighestFromFloor()) {
            try {
                willOpen = elevator.upElevator();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (elevator.getFloor() >= elevator.getMaxDestination()
                    && elevator.getFloor() >= requestQueue.getHighestFromFloor()) {
                if (ifChangeWay(true, elevator.getFloor(), elevator)) {
                    break;
                }
            } else {
                judgeOpen(willOpen, elevator.getFloor(), true, elevator);
            }
        }
        if (elevator.getPeopleNum() != 0) {
            goDown(elevator);
        }
    }

    //开关门，上下人
    public void judgeOpen(boolean willOpen, int nowFloor, boolean isUp, Elevator elevator) {
        boolean wantIn = false;

        synchronized (requestQueue) {
            for (PersonRequest entry : requestQueue.getPersonRequests()) {
                if (entry.getFromFloor() == nowFloor) {
                    if ((((entry.getToFloor() - entry.getFromFloor()) > 0) == isUp)) {
                        wantIn = true;
                        break;
                    }
                }
            }
        }
        //若不超载且有人在，或者里面的人要下
        if ((elevator.getPeopleNum() < elevator.getMaxLoad() && wantIn) || willOpen) {
            //sleep for 400ms
            if (!hasOpen[nowFloor - 1] || willOpen) {
                hasOpen[nowFloor - 1] = true;
                elevator.openElevator();
                synchronized (requestQueue) {
                    ArrayList<PersonRequest> newPersonRequest =
                            new ArrayList<>(requestQueue.getPersonRequests());
                    for (PersonRequest entry : requestQueue.getPersonRequests()) {
                        if (entry.getFromFloor() == nowFloor &&
                                elevator.getPeopleNum() < elevator.getMaxLoad()) {
                            if ((isUp && entry.getToFloor() > nowFloor) ||
                                    (!isUp && entry.getToFloor() < nowFloor)) {
                                elevator.addPersonRequest(entry, isUp);
                                newPersonRequest.remove(entry);
                            }
                        }
                    }
                    requestQueue.setPersonRequests(newPersonRequest);
                }
                elevator.closeElevator();
                hasOpen[nowFloor - 1] = false;
            }
        }
        //notifyAll();
    }

    public boolean ifChangeWay(boolean initWay, int nowFloor, Elevator elevator) {
        //true == up, false == down
        //judge whether to change the way
        int flag = 0;
        elevator.openElevator();
        synchronized (requestQueue) {
            ArrayList<PersonRequest> newPersonRequest =
                    new ArrayList<>(requestQueue.getPersonRequests());
            if (initWay) {
                for (PersonRequest entry : requestQueue.getPersonRequests()) {
                    if (entry.getFromFloor() == nowFloor &&
                            entry.getToFloor() > entry.getFromFloor()) {
                        flag = 1;
                        if (elevator.getPeopleNum() < elevator.getMaxLoad()) {
                            elevator.addPersonRequest(entry, true);
                            newPersonRequest.remove(entry);
                        }
                    }
                }
                if (flag == 0) {
                    elevator.clear();
                    for (PersonRequest entry : requestQueue.getPersonRequests()) {
                        if (entry.getFromFloor() == nowFloor
                                && entry.getToFloor() < entry.getFromFloor()) {
                            if (elevator.getPeopleNum() < elevator.getMaxLoad()) {
                                elevator.addPersonRequest(entry, false);
                                newPersonRequest.remove(entry);
                            }
                        }
                    }
                }
            } else {
                for (PersonRequest entry : requestQueue.getPersonRequests()) {
                    if (entry.getFromFloor() == nowFloor &&
                            entry.getToFloor() < entry.getFromFloor()) {
                        flag = 1;
                        if (elevator.getPeopleNum() < elevator.getMaxLoad()) {
                            elevator.addPersonRequest(entry, false);
                            newPersonRequest.remove(entry);
                        }
                    }
                }
                if (flag == 0) {
                    elevator.clear();
                    for (PersonRequest entry : requestQueue.getPersonRequests()) {
                        if (entry.getFromFloor() == nowFloor
                                && entry.getToFloor() > entry.getFromFloor()) {
                            if (elevator.getPeopleNum() < elevator.getMaxLoad()) {
                                elevator.addPersonRequest(entry, true);
                                newPersonRequest.remove(entry);
                            }
                        }
                    }
                }
            }
            requestQueue.setPersonRequests(newPersonRequest);
        }
        elevator.closeElevator();
        return (flag == 0);
    }
}
