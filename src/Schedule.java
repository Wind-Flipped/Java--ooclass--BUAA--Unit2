import com.oocourse.elevator3.PersonRequest;

import java.util.ArrayList;
import java.util.HashMap;

public class Schedule implements Runnable {
    private final RequestQueue waitQueue;
    private final ArrayList<RequestQueue> verticalQueues;
    private final ArrayList<RequestQueue> horizontalQueues;
    private static Schedule instance;
    public static final HashMap<Integer,ArrayList<Integer>> CONDITION = new HashMap<>();
    public static final HashMap<Integer, ArrayList<PersonRequest>> EXCHANGE_LIST = new HashMap<>();

    public Schedule(RequestQueue waitQueue, ArrayList<RequestQueue> verticalQueues,
                    ArrayList<RequestQueue> horizontalQueues) {
        this.waitQueue = waitQueue;
        this.verticalQueues = verticalQueues;
        this.horizontalQueues = horizontalQueues;
        instance = this;
        //CONDITION = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            ArrayList<Integer> integers = new ArrayList<>();
            CONDITION.put(i,integers);
        }
        CONDITION.get(1).add(31);
        //CONDITION.put(1,null);
        //EXCHANGE_LIST = new HashMap<>();
    }

    public static synchronized Schedule getInstance() {
        return instance;
    }

    @Override
    public void run() {
        while (true) {
            if (waitQueue.isEmpty() && waitQueue.isEnd()) {
                //需要等待流水线完成
                preReturn();
                for (RequestQueue processingQueue : verticalQueues) {
                    processingQueue.setEnd(true);
                }
                for (RequestQueue processingQueue : horizontalQueues) {
                    processingQueue.setEnd(true);
                }
                //OutputThread.println("Schedule End!!!");
                break;
            }
            PersonRequest personRequest = waitQueue.getOneRequest();
            if (personRequest == null) {
                continue;
            }
            if (personRequest.getFromBuilding() == personRequest.getToBuilding()) {
                verticalQueues.get(personRequest.getFromBuilding() - 'A')
                        .addPersonRequest(personRequest);
                //TimableOutput.println(personRequest.getPersonId() + " is in the queue!");
            } else {
                addRequest(personRequest);
                //TimableOutput.println(personRequest.getPersonId() + " is in the queue!");
            }
        }
    }

    public synchronized void preReturn() {
        while (!EXCHANGE_LIST.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean canOpen(int switchInfo, char building1, char building2) {
        return (((switchInfo >> (building1 - 'A')) & 1) == 1) &&
                (((switchInfo >> (building2 - 'A')) & 1) == 1);
    }

    public int abs(int value) {
        return value > 0 ? value : -value;
    }

    public int findSuitableFloor(PersonRequest personRequest) {
        int suitableFloor = 1;//最佳层
        int crossFloor = 20;//要跨越几层
        int nowFloor;
        char fromBuilding = personRequest.getFromBuilding();
        char toBuilding = personRequest.getToBuilding();
        int fromFloor = personRequest.getFromFloor();
        int toFloor = personRequest.getToFloor();
        int personId = personRequest.getPersonId();
        if ((personId & 1) == 1) {
            for (int i = 1; i <= 10; i++) {
                if (!CONDITION.get(i).isEmpty()) {
                    for (int j = 0; j < CONDITION.get(i).size(); j++) {
                        if (canOpen(CONDITION.get(i).get(j), fromBuilding, toBuilding)) {
                            nowFloor = abs(fromFloor - i) + abs(toFloor - i);
                            if (nowFloor <= crossFloor) {
                                crossFloor = nowFloor;
                                suitableFloor = i;
                                break;
                            }
                        }
                    }
                    if (suitableFloor == fromFloor || suitableFloor == toFloor) {
                        break;
                    }
                }
            }
        } else {
            for (int i = 10; i >= 1; i--) {
                if (!CONDITION.get(i).isEmpty()) {
                    for (int j = 0; j < CONDITION.get(i).size(); j++) {
                        if (canOpen(CONDITION.get(i).get(j), fromBuilding, toBuilding)) {
                            nowFloor = abs(fromFloor - i) + abs(toFloor - i);
                            if (nowFloor <= crossFloor) {
                                crossFloor = nowFloor;
                                suitableFloor = i;
                                break;
                            }
                        }
                    }
                    if (suitableFloor == fromFloor || suitableFloor == toFloor) {
                        break;
                    }
                }
            }
        }
        return suitableFloor;
    }

    public synchronized void addRequest(PersonRequest personRequest) {
        ArrayList<PersonRequest> personRequests = new ArrayList<>();
        char fromBuilding = personRequest.getFromBuilding();
        char toBuilding = personRequest.getToBuilding();
        int fromFloor = personRequest.getFromFloor();
        int toFloor = personRequest.getToFloor();
        int personId = personRequest.getPersonId();
        int suitableFloor = findSuitableFloor(personRequest);
        if (fromFloor == suitableFloor && toFloor == suitableFloor) {
            //直接坐横向电梯
            horizontalQueues.get(fromFloor - 1)
                    .addPersonRequest(personRequest);
            return;
        } else if (fromFloor == suitableFloor) {
            PersonRequest personRequest1 = new PersonRequest(
                    fromFloor, fromFloor, fromBuilding, toBuilding, personId);
            PersonRequest personRequest2 = new PersonRequest(
                    fromFloor, toFloor, toBuilding, toBuilding, personId);
            horizontalQueues.get(fromFloor - 1).addPersonRequest(personRequest1);
            //ArrayList<PersonRequest> personRequests = new ArrayList<>();
            personRequests.add(personRequest2);
            //EXCHANGE_LIST.put(personId, personRequests);
        } else if (toFloor == suitableFloor) {
            PersonRequest personRequest1 = new PersonRequest(
                    fromFloor, toFloor, fromBuilding, fromBuilding, personId);
            PersonRequest personRequest2 = new PersonRequest(
                    toFloor, toFloor, fromBuilding, toBuilding, personId);
            verticalQueues.get(fromBuilding - 'A').addPersonRequest(personRequest1);
            //ArrayList<PersonRequest> personRequests = new ArrayList<>();
            personRequests.add(personRequest2);
            //EXCHANGE_LIST.put(personId, personRequests);
        } else {
            PersonRequest personRequest1 = new PersonRequest(
                    fromFloor, suitableFloor, fromBuilding, fromBuilding, personId);
            PersonRequest personRequest2 = new PersonRequest(
                    suitableFloor, suitableFloor, fromBuilding, toBuilding, personId);
            PersonRequest personRequest3 = new PersonRequest(
                    suitableFloor, toFloor, toBuilding, toBuilding, personId);
            verticalQueues.get(fromBuilding - 'A').addPersonRequest(personRequest1);
            //ArrayList<PersonRequest> personRequests = new ArrayList<>();
            personRequests.add(personRequest2);
            personRequests.add(personRequest3);
            //EXCHANGE_LIST.put(personId, personRequests);
        }
        EXCHANGE_LIST.put(personId, personRequests);
    }

    public synchronized void notifyRequest(int id) {
        if (!EXCHANGE_LIST.containsKey(id)) {
            return;
        }
        PersonRequest personRequest = EXCHANGE_LIST.get(id).get(0);
        EXCHANGE_LIST.get(id).remove(0);
        if (EXCHANGE_LIST.get(id).isEmpty()) {
            EXCHANGE_LIST.remove(id);
        }
        if (personRequest.getFromBuilding() == personRequest.getToBuilding()) {
            //纵向电梯
            verticalQueues.get(personRequest.getFromBuilding() - 'A')
                    .addPersonRequest(personRequest);
        } else {
            horizontalQueues.get(personRequest.getFromFloor() - 1).addPersonRequest(personRequest);
        }
        notifyAll();
    }
}
