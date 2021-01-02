package ro.ubb.pdp;

import mpi.MPI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Message implements Serializable {}

class VariableRelatedMessage extends Message {
  public String variable;

  VariableRelatedMessage(String variable) {
    this.variable = variable;
  }
}

class UpdateMessage extends VariableRelatedMessage {
  public int value;

  UpdateMessage(String variable, int value) {
    super(variable);
    this.value = value;
  }

  @Override
  public String toString() {
    return "UpdateMessage{" + "variable='" + variable + '\'' + ", value=" + value + '}';
  }
}

class SubscribeMessage extends VariableRelatedMessage {
  public int rank;

  SubscribeMessage(String variable, int rank) {
    super(variable);
    this.rank = rank;
  }

  @Override
  public String toString() {
    return "SubscribeMessage{" + "variable='" + variable + '\'' + ", rank=" + rank + '}';
  }
}

class ChangeMessage extends VariableRelatedMessage {
  public int oldValue;
  public int newValue;

  ChangeMessage(String variable, int oldValue, int newValue) {
    super(variable);
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  @Override
  public String toString() {
    return "ChangeMessage{"
        + "variable='"
        + variable
        + '\''
        + ", oldValue="
        + oldValue
        + ", newValue="
        + newValue
        + '}';
  }
}

class ExitMessage extends Message {}

public class DSM {

  public int a = 2, b = 4, c = 8;
  public Map<String, List<Integer>> subscribers = new HashMap<>();

  public DSM() {
    subscribers.put("a", new ArrayList<>());
    subscribers.put("b", new ArrayList<>());
    subscribers.put("c", new ArrayList<>());
  }

  public void updateVariable(String variable, int value) {
    this.setVariable(variable, value);
    UpdateMessage updateMessage = new UpdateMessage(variable, value);
    this.sendMessageToSubscribers(variable, updateMessage);
  }

  public void setVariable(String variable, int value) {

    if (variable.equals("a")) a = value;
    if (variable.equals("b")) b = value;
    if (variable.equals("c")) c = value;
  }

  public void subscribeTo(String variable) {
    this.subscribers.get(variable).add(MPI.COMM_WORLD.Rank());
    this.sendAll(new SubscribeMessage(variable, MPI.COMM_WORLD.Rank()));
  }

  public void subscribeOther(String variable, int rank) {
    this.subscribers.get(variable).add(rank);
  }

  public void sendMessageToSubscribers(String variable, Message message) {
    for (int i = 0; i < MPI.COMM_WORLD.Size(); i++) {
      if (MPI.COMM_WORLD.Rank() == i) continue;
      if (!this.isSubscribedToVariable(variable, i)) continue;
      Message[] messageWrapper = new Message[1];
      messageWrapper[0] = message;
      MPI.COMM_WORLD.Send(messageWrapper, 0, 1, MPI.OBJECT, i, 0);
    }
  }

  private boolean isSubscribedToVariable(String variable, int rank) {
    return subscribers.get(variable).contains(rank);
  }

  public void checkAndReplace(String variable, int value, int newValue) {
    switch (variable) {
      case "a":
        if (a == value) {
          updateVariable("a", newValue);
        }
        break;
      case "b":
        if (b == value) {
          updateVariable("b", newValue);
        }
        break;
      case "c":
        if (c == value) {
          updateVariable("c", newValue);
        }
        break;
    }
  }

  public void close() {
    this.sendAll(new ExitMessage());
  }

  private void sendAll(Message message) {
    for (int i = 0; i < MPI.COMM_WORLD.Size(); i++) {
      if (MPI.COMM_WORLD.Rank() == i) continue;
      Message[] messageWrapper = new Message[1];
      messageWrapper[0] = message;
      MPI.COMM_WORLD.Send(messageWrapper, 0, 1, MPI.OBJECT, i, 0);
    }
  }
}
