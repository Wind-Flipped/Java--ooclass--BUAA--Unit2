import com.oocourse.elevator3.ElevatorInput;
import com.oocourse.elevator3.ElevatorRequest;
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;

import java.io.IOException;
import java.util.ArrayList;

public class InputThread extends Thread {
    private final RequestQueue waitQueue;
    private final ArrayList<VerticalProcess> verticalProcesses;
    private final ArrayList<HorizontalProcess> horizontalProcesses;

    public InputThread(ArrayList<VerticalProcess> verticalProcesses,
                       ArrayList<HorizontalProcess> horizontalProcesses,
                       RequestQueue waitQueue) {
        this.waitQueue = waitQueue;
        this.verticalProcesses = verticalProcesses;
        this.horizontalProcesses = horizontalProcesses;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            // when request == null
            // it means there are no more lines in stdin
            if (request == null) {
                waitQueue.setEnd(true);
                //OutputThread.println("Input End!!!");//just a signal
                break;
            } else {
                // a new valid request
                if (request instanceof PersonRequest) {
                    // a PersonRequest
                    // your code here
                    waitQueue.addPersonRequest((PersonRequest) request);
                    //System.out.println("A PersonRequest:    " + request);

                } else if (request instanceof ElevatorRequest) {
                    // an ElevatorRequest
                    // your code here
                    if (((ElevatorRequest) request).getType().equals("building")) {
                        //纵向电梯
                        Elevator myRunnable = new Elevator(
                                ((ElevatorRequest) request).getElevatorId(),
                                ((ElevatorRequest) request).getBuilding(),
                                1,
                                verticalProcesses.get(
                                        (((ElevatorRequest) request).getBuilding()) - 'A'),
                                ((ElevatorRequest) request).getCapacity(),
                                ((ElevatorRequest) request).getSpeed(),
                                ((ElevatorRequest) request).getSwitchInfo());
                        Thread elevator = new Thread(myRunnable,
                                "elevator" + ((ElevatorRequest) request).getElevatorId());
                        elevator.start();
                    } else {
                        //横向电梯
                        Elevator myRunnable = new Elevator(
                                ((ElevatorRequest) request).getElevatorId(),
                                'A',
                                ((ElevatorRequest) request).getFloor(),
                                horizontalProcesses.get(
                                        ((ElevatorRequest) request).getFloor() - 1),
                                ((ElevatorRequest) request).getCapacity(),
                                ((ElevatorRequest) request).getSpeed(),
                                ((ElevatorRequest) request).getSwitchInfo());
                        Thread elevator = new Thread(myRunnable,
                                "elevator" + ((ElevatorRequest) request).getElevatorId());
                        Schedule.CONDITION.get(((ElevatorRequest) request).getFloor()).
                                add(((ElevatorRequest) request).getSwitchInfo());
                        elevator.start();
                    }
                    //System.out.println("An ElevatorRequest: " + request);

                }
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
