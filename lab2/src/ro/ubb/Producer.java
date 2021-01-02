package ro.ubb;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Producer implements Runnable {

    private BlockingQueue<Integer> numbersQueue;

    private ReentrantLock lock;
    private Condition notFull;
    private Condition notEmpty;
    private ArrayList<Integer> firstVector;
    private ArrayList<Integer> secondVector;
    private Random random;


    public Producer(BlockingQueue<Integer> numbersQueue, ReentrantLock lock, Condition notFull, Condition notEmpty, ArrayList<Integer> firstVector, ArrayList<Integer> secondVector, Random random) {
        this.numbersQueue = numbersQueue;
        this.lock = lock;
        this.notFull = notFull;
        this.notEmpty = notEmpty;
        this.firstVector = firstVector;
        this.secondVector = secondVector;
        this.random = random;
    }

    @Override
    public void run() {
        int i = 0;
        while (i < firstVector.size()) {

            try{
                Integer val = firstVector.get(i) * secondVector.get(i);
                numbersQueue.put(firstVector.get(i)*secondVector.get(i));
                System.out.println("Added an element to the queue " + val);
                i++;
            }
             catch (InterruptedException e) {
                e.printStackTrace();
            }
//            lock.lock();
//            try {
//                while (!numbersQueue.isEmpty()) {
//                    System.out.println("Buffer in full, waiting");
//                    notFull.await();
//                }
//                Integer val = firstVector.get(i) * secondVector.get(i);
//                if (numbersQueue.offer(val)) {
//                    System.out.println("Added an element to the queue");
//                    notEmpty.signal();
//                    i++;
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } finally {
////                lock.unlock();
//            }
        }
        System.out.println("producer finished");
    }
}
