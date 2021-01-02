package ro.ubb.pdp;

import mpi.MPI;

import java.util.ArrayDeque;
import java.util.Queue;

public class Main {

  public static void listener(DSM dsm) {
    while (true) {
      System.out.println("Rank" + MPI.COMM_WORLD.Rank() + " waiting");
      Message message = readMessage();

      if (message instanceof ExitMessage) break;
      else if(message instanceof UpdateMessage){
        System.out.println("Current rank " + MPI.COMM_WORLD.Rank() + " received message " + message);
        UpdateMessage updateMessage = (UpdateMessage) message;
        dsm.setVariable(updateMessage.variable,updateMessage.value);
      }
      else if (message instanceof SubscribeMessage){
        System.out.println("Current rank " + MPI.COMM_WORLD.Rank() + " received message " + message);
        SubscribeMessage updateMessage = (SubscribeMessage) message;
        dsm.subscribeOther(updateMessage.variable,updateMessage.rank);
      }
      printVariables(dsm);
    }
  }

  private static Message readMessage() {
    Message[] messageWrapper = new Message[1];
    MPI.COMM_WORLD.Recv(messageWrapper, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, MPI.ANY_TAG);
    return messageWrapper[0];
  }

  private static void printVariables(DSM dsm) {
    StringBuilder builder = new StringBuilder();
    builder
        .append("Rank ")
        .append(MPI.COMM_WORLD.Rank())
        .append(" a= ")
        .append(dsm.a)
        .append(" b= ")
        .append(dsm.b)
        .append(" c= ")
        .append(dsm.c)
        .append(" subs: ");
    for (String var : dsm.subscribers.keySet()) {
      builder.append(var).append(": [ ");
      for (int rank : dsm.subscribers.get(var)) {
        builder.append(rank).append(" ");
      }

      builder.append("] ");
    }

    builder.append("\n");

    System.out.println(builder);
  }

  public static void main(String[] args) {

    MPI.Init(args);

    int rank = MPI.COMM_WORLD.Rank();
    int size = MPI.COMM_WORLD.Size();

    DSM dsm = new DSM();

    if (rank == 0) {

      Thread thread = new Thread(() -> listener(dsm));
      thread.start();

      Queue<String> commands = new ArrayDeque<>();

      commands.add("1");
      commands.add("a");
      commands.add("10");

      commands.add("1");
      commands.add("b");
      commands.add("20");

      commands.add("2");
      commands.add("a");
      commands.add("10");
      commands.add("20");

      commands.add("2");
      commands.add("b");
      commands.add("20");
      commands.add("10");

      commands.add("0");
      commands.add("b");
      commands.add("20");
      commands.add("10");

      boolean hasExited = false;

      dsm.subscribeTo("a");
      dsm.subscribeTo("b");
      dsm.subscribeTo("c");


      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      while (!hasExited) {
//        printMenu();

        try {
//          System.out.println("reading");
          String inputData = commands.poll();
          System.out.println("\n=\n=\nCurrent command " +inputData+"\n=\n=\n");
          switch (inputData) {
            case "0" -> {
              dsm.close();
              thread.join();
              hasExited = true;
              break;
            }
            case "1" -> {
              System.out.println("Choose from variables: (a, b, c)");
              String answer = commands.poll();
              System.out.println(answer);
              if (!(answer.equals("a") || answer.equals("b") || answer.equals("c")))
                System.out.println("Invalid input!");
              else {
                System.out.println("Write the value");
                int value = Integer.parseInt(commands.poll());
                System.out.println(value);
                dsm.updateVariable(answer, value);
                printVariables(dsm);
              }
              break;
            }
            case "2" -> {
              System.out.println("Choose from variables: (a, b, c)");
              String answer = commands.poll();
              System.out.println(answer);
              if (!(answer.equals("a") || answer.equals("b") || answer.equals("c")))
                System.out.println("Invalid input!");

              System.out.println("Old value: ");
              int oldValue = Integer.parseInt(commands.poll());
              System.out.println(oldValue);
              System.out.println("New value: ");
              int newValue = Integer.parseInt(commands.poll());
              System.out.println(newValue);
              dsm.checkAndReplace(answer, oldValue, newValue);
              break;
            }
          }
          Thread.sleep(5000);
        }  catch (InterruptedException e) {
          hasExited = true;
        } catch (NumberFormatException e) {
          System.out.println("Wrong input type");
        }
      }

    } else if (rank == 1) {

      Thread thread = new Thread(() -> listener(dsm));
      thread.start();
      dsm.subscribeTo("a");
      try {
        thread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

    } else if (rank == 2) {
      Thread thread = new Thread(() -> listener(dsm));
      thread.start();
      dsm.subscribeTo("b");
      try {
        thread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    MPI.Finalize();
  }

  private static void printMenu() {
    System.out.println("===\n1. Set variable\n2. Check and replace variable\n0. Exit\n===");
  }
}
