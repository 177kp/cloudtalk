package com.zhangwuji.im.callback;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class ListenerQueue {

    private static ListenerQueue listenerQueue = new ListenerQueue();
    public static ListenerQueue instance(){
        return listenerQueue;
    }

    private volatile  boolean stopFlag = false;
    private volatile  boolean hasTask = false;


    //callback 队列
    private Map<Integer,Packetlistener> callBackQueue = new ConcurrentHashMap<>();
   // private Handler timerHandler = new Handler();
    Timer timer = new Timer();

    public void onStart(){
        stopFlag = false;
        startTimer();
    }
    public void onDestory(){
        callBackQueue.clear();
        stopTimer();
    }

    //以前是TimerTask处理方式
    private void startTimer() {
        if(!stopFlag && hasTask == false) {
            hasTask = true;
//            timerHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    timerImpl();
//                    hasTask = false;
//                    startTimer();
//                }
//            }, 5 * 1000);
            
            timer.schedule(new TimerTask() {
                    public void run() {
                    	timerImpl();
                        hasTask = false;
                        startTimer();
                    }
            }, 5 * 1000);
            
        }
    }

    private void stopTimer(){
        stopFlag = true;
    }

    private void timerImpl() {
        long currentRealtime =   System.currentTimeMillis();//SystemClock.elapsedRealtime();

        for (java.util.Map.Entry<Integer, Packetlistener> entry : callBackQueue.entrySet()) {

            Packetlistener packetlistener = entry.getValue();
            Integer seqNo = entry.getKey();
            long timeRange = currentRealtime - packetlistener.getCreateTime();

            try {
                if (timeRange >= packetlistener.getTimeOut()) {
                    Packetlistener listener = pop(seqNo);
                    if (listener != null) {
                        listener.onTimeout();
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    public void push(int seqNo,Packetlistener packetlistener){
        if(seqNo <=0 || null==packetlistener){
            return;
        }
        callBackQueue.put(seqNo,packetlistener);
    }


    public Packetlistener pop(int seqNo){
        synchronized (ListenerQueue.this) {
            if (callBackQueue.containsKey(seqNo)) {
                Packetlistener packetlistener = callBackQueue.remove(seqNo);
                return packetlistener;
            }
            return null;
        }
    }
}
