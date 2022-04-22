import com.oocourse.elevator3.PersonRequest;

import java.util.ArrayList;

public class RequestQueue {
    private ArrayList<PersonRequest> personRequests;
    private boolean isEnd;

    public RequestQueue() {
        personRequests = new ArrayList<>();
        isEnd = false;
    }

    public synchronized void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
        notifyAll();
    }

    public synchronized boolean isEnd() {
        notifyAll();
        return isEnd;
    }

    public synchronized boolean isEmpty() {
        notifyAll();
        return personRequests.isEmpty();
    }

    public synchronized void addPersonRequest(PersonRequest personRequest) {
        personRequests.add(personRequest);
        notifyAll();
    }

    public ArrayList<PersonRequest> getPersonRequests() {
        return personRequests;
    }

    public void setPersonRequests(ArrayList<PersonRequest> personRequests) {
        synchronized (this.personRequests) {
            this.personRequests = personRequests;
            notifyAll();
        }
    }

    public synchronized PersonRequest getOneRequest() {
        while (personRequests.isEmpty() && !isEnd) {
            try {
                //System.out.println("begin wait");
                wait();
                //System.out.println("end wait");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (personRequests.isEmpty()) {
            return null;
        }
        PersonRequest personRequest = personRequests.get(0);
        personRequests.remove(0);
        notifyAll();
        return personRequest;
    }

    public synchronized PersonRequest findOneRequest() {
        while (personRequests.isEmpty() && !isEnd) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (personRequests.isEmpty()) {
            return null;
        }
        PersonRequest personRequest = personRequests.get(0);
        notifyAll();
        return personRequest;
    }

    public int getLowestFromFloor() {
        int lowestFromFloor = Elevator.MAX_FLOOR;
        if (personRequests.isEmpty()) {
            return lowestFromFloor;
        }
        synchronized (personRequests) {
            for (PersonRequest entry : personRequests) {
                if (entry.getFromFloor() < lowestFromFloor) {
                    lowestFromFloor = entry.getFromFloor();
                }
            }
        }
        return lowestFromFloor;
    }

    public int getHighestFromFloor() {
        int highestFromFloor = Elevator.MIN_FLOOR;
        if (personRequests.isEmpty()) {
            return highestFromFloor;
        }
        synchronized (personRequests) {
            for (PersonRequest entry : personRequests) {
                if (entry.getFromFloor() > highestFromFloor) {
                    highestFromFloor = entry.getFromFloor();
                }
            }
        }
        return highestFromFloor;
    }
}