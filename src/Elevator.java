import com.oocourse.elevator3.PersonRequest;

import java.util.ArrayList;

public class Elevator implements Runnable {
    private final int id;
    private char buildingId;
    private ArrayList<PersonRequest> peopleInElevator;
    private int floor;
    private final Process process;
    //private Status status
    private int peopleNum;
    private boolean needOpen;
    private int maxDestination;
    private int minDestination;
    private final int speed;
    //*1000
    private final int switchInfo;
    private final int maxLoad;
    public static final int MAX_FLOOR = 10;
    public static final int MIN_FLOOR = 1;

    //define A->B is right,A->E is left

    public Elevator(int id, char buildingId, int floor, Process process,
                    int maxLoad,double speed,int switchInfo) {
        needOpen = false;
        this.process = process;
        this.buildingId = buildingId;
        peopleNum = 0;
        this.id = id;
        peopleInElevator = new ArrayList<>();
        this.floor = floor;
        maxDestination = 1;
        minDestination = 10;
        this.maxLoad = maxLoad;
        this.speed = (int) (speed * 1000);
        this.switchInfo = switchInfo;
    }

    public int getMaxLoad() {
        return maxLoad;
    }

    public int getSwitchInfo() {
        return switchInfo;
    }

    public int getFloor() {
        return floor;
    }

    public char getBuildingId() {
        return buildingId;
    }

    public int getPeopleNum() {
        return peopleNum;
    }

    public int getMaxDestination() {
        return maxDestination;
    }

    public int getMinDestination() {
        return minDestination;
    }

    public void clear() {
        maxDestination = 1;
        minDestination = 10;
    }

    public synchronized boolean rightElevator() {
        try {
            Thread.sleep(speed);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        buildingId++;
        buildingId = buildingId == 'F' ? 'A' : buildingId;
        OutputThread.println("ARRIVE-" + buildingId + "-" + floor + "-" + id);
        //judge if this is the destination
        for (PersonRequest entry : peopleInElevator) {
            if (buildingId == entry.getToBuilding()) {
                needOpen = true;
            }
        }
        notifyAll();
        return needOpen;
    }

    public synchronized boolean leftElevator() {
        try {
            Thread.sleep(speed);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        buildingId--;
        buildingId = buildingId < 'A' ? 'E' : buildingId;
        OutputThread.println("ARRIVE-" + buildingId + "-" + floor + "-" + id);
        //judge if this is the destination
        for (PersonRequest entry : peopleInElevator) {
            if (buildingId == entry.getToBuilding()) {
                needOpen = true;
            }
        }
        notifyAll();
        return needOpen;
    }

    public synchronized boolean upElevator() throws InterruptedException {
        try {
            Thread.sleep(speed);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        floor++;
        OutputThread.println("ARRIVE-" + buildingId + "-" + floor + "-" + id);
        //judge if this is the destination
        for (PersonRequest entry : peopleInElevator) {
            if (floor == entry.getToFloor()) {
                needOpen = true;
            }
        }
        notifyAll();
        return needOpen;
    }

    public synchronized boolean downElevator() throws InterruptedException {
        try {
            Thread.sleep(speed);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        floor--;
        OutputThread.println("ARRIVE-" + buildingId + "-" + floor + "-" + id);
        //judge if this is the destination
        for (PersonRequest entry : peopleInElevator) {
            if (floor == entry.getToFloor()) {
                needOpen = true;
            }
        }
        notifyAll();
        return needOpen;
    }

    public synchronized void addPersonRequest(PersonRequest personRequest, boolean isUp) {
        peopleInElevator.add(personRequest);
        peopleNum++;
        if (isUp) {
            if (maxDestination < personRequest.getToFloor()) {
                maxDestination = personRequest.getToFloor();
            }
        } else {
            if (minDestination > personRequest.getToFloor()) {
                minDestination = personRequest.getToFloor();
            }
        }
        OutputThread.println("IN-" + personRequest.getPersonId() + "-" + buildingId + "-"
                + floor + "-" + id);
        notifyAll();
    }

    public synchronized void addPersonRequest(PersonRequest personRequest) {
        peopleInElevator.add(personRequest);
        peopleNum++;
        OutputThread.println("IN-" + personRequest.getPersonId() + "-" + buildingId + "-"
                + floor + "-" + id);
        notifyAll();
    }

    public synchronized void removePersonRequest() {
        ArrayList<PersonRequest> newPersonRequest = new ArrayList<>(peopleInElevator);
        for (PersonRequest entry : peopleInElevator) {
            if (floor == entry.getToFloor() && buildingId == entry.getToBuilding()) {
                OutputThread.println("OUT-" + entry.getPersonId() + "-" + buildingId + "-"
                        + floor + "-" + id);
                Schedule.getInstance().notifyRequest(entry.getPersonId());
                peopleNum--;
                newPersonRequest.remove(entry);
            }
        }
        peopleInElevator = newPersonRequest;
        notifyAll();
    }

    public synchronized void openElevator() {
        try {
            OutputThread.println("OPEN-" + buildingId + "-" + floor + "-" + id);
            if (needOpen) {
                removePersonRequest();
            }
            Thread.sleep(400);

            needOpen = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        notifyAll();
    }

    public synchronized void closeElevator() {
        OutputThread.println("CLOSE-" + buildingId + "-" + floor + "-" + id);
        notifyAll();
    }

    @Override
    public void run() {
        process.run(this);
    }
}
