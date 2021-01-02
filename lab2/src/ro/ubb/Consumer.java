package ro.ubb;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Consumer implements Runnable {

    private BlockingQueue<Integer> numbersQueue;

    private ReentrantLock lock;
    private Random random;
    private Condition notFull;
    private Condition notEmpty;


    private final int vectorCapacity;
    Integer result;

    public Consumer(BlockingQueue<Integer> buffer,ReentrantLock lock,Condition notFull,Condition notEmpty, int vectorCapacity,Random random) {
        this.notFull = notFull;
        this.notEmpty = notEmpty;
        this.vectorCapacity = vectorCapacity;
        this.numbersQueue = buffer;
        this.lock = lock;
        this.random = random;
        result = 0;
    }

    @Override
    public void run() {
        int i =0;
        while (i < vectorCapacity){
            try {
                Integer value = numbersQueue.take();
                System.out.println("Value obtained " + value);
                result += value;
                i++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("result is: " + result);
//        int i = 0;
//        while (i < vectorCapacity) {
//            lock.lock();
//            try {
//                while (numbersQueue.isEmpty()){
//                    System.out.println("Buffer in full, waiting");
//                    notEmpty.await();
//                }
//                Integer val = numbersQueue.poll();
//                if(val != null){
//                    System.out.println("Value obtained");
//                    notFull.signal();
//                    i++;
//                    result += val;
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            finally {
//                lock.unlock();
//            }
//
//            /*try {
//                Thread.sleep(random.nextInt(100));
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }*/
//
//
//        }
//        System.out.println("Result is: " + result + "\nConsumer finished");
    }
}
