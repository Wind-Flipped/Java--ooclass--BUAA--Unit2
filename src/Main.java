import com.oocourse.TimableOutput;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();  // 初始化时间戳

        ArrayList<VerticalProcess> verticalProcesses = new ArrayList<>();
        ArrayList<HorizontalProcess> horizontalProcesses = new ArrayList<>();
        ArrayList<RequestQueue> processingVerticalQueues = new ArrayList<>();
        ArrayList<RequestQueue> processingHorizontalQueues = new ArrayList<>();

        //五栋大楼
        int n = 5;
        for (int i = 1; i <= n; i++) {
            RequestQueue parallelQueue = new RequestQueue();
            processingVerticalQueues.add(parallelQueue);
            VerticalProcess verticalProcess = new VerticalProcess(parallelQueue, i);
            verticalProcesses.add(verticalProcess);
        }

        //初始五部电梯，五个线程
        for (int i = 1; i <= n; i++) {
            Elevator myRunnable = new Elevator(i, (char) ('A' + i - 1),
                    1, verticalProcesses.get(i - 1), 8, 0.6, 0);
            Thread elevator = new Thread(myRunnable, "Elevator " + i);
            elevator.start();
        }
        n = 10;
        for (int i = 1; i <= n; i++) {
            RequestQueue parallelQueue = new RequestQueue();
            processingHorizontalQueues.add(parallelQueue);
            HorizontalProcess horizontalProcess = new HorizontalProcess(parallelQueue, i);
            horizontalProcesses.add(horizontalProcess);
        }
        //初始1部横向电梯
        Elevator runnable = new Elevator(6, 'A',
                1, horizontalProcesses.get(0), 8, 0.6, 31);
        Thread elevator1 = new Thread(runnable, "Elevator 6");
        elevator1.start();
        //Schedule 决定是横向还是纵向
        RequestQueue waitQueue = new RequestQueue();
        Schedule myRunnable = new Schedule(waitQueue,
                processingVerticalQueues, processingHorizontalQueues);
        Thread schedule = new Thread(myRunnable, "Schedule");
        schedule.start();

        InputThread inputThread = new InputThread(
                verticalProcesses, horizontalProcesses, waitQueue);
        inputThread.start();
    }
}
