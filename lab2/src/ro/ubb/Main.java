package ro.ubb;

import java.lang.reflect.Array;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) {
        final int maxVectorSize = 10;

        ArrayList<Integer> firstVector = IntStream.range(0, maxVectorSize).boxed().collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Integer> secondVector = IntStream.range(0,maxVectorSize).boxed().collect(Collectors.toCollection(ArrayList::new));

        BlockingQueue<Integer> buffer = new ArrayBlockingQueue<>(10);

        ReentrantLock lock = new ReentrantLock();
        Condition notFull = lock.newCondition();
        Condition notEmpty = lock.newCondition();


        Random random = new Random();
        Consumer consumer = new Consumer(buffer,lock,notFull,notEmpty,maxVectorSize,random);
        Producer producer = new Producer(buffer,lock,notFull,notEmpty,firstVector,secondVector,random);

        Thread threadOne = new Thread(producer);
        Thread threadTwo = new Thread(consumer);
        threadOne.start();
        threadTwo.start();
        try {
            threadOne.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            threadTwo.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
