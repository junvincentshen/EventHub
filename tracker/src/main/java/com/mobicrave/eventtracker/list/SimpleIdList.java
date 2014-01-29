package com.mobicrave.eventtracker.list;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class SimpleIdList implements IdList, Serializable {
  private static final long serialVersionUID = 8806039426767633834L;

  private final String filename;
  private long[] list;
  private int numRecords;

  public SimpleIdList(String filename, long[] list, int numRecords) {
    this.filename = filename;
    this.list = list;
    this.numRecords = numRecords;
  }

  @Override
  public void add(long id) {
    if (numRecords == list.length) {
      long[] newList = new long[list.length * 2];
      System.arraycopy(list, 0, newList, 0, list.length);
      list = newList;
    }
    list[numRecords++] = id;
  }

  @Override
  public Iterator subList(long firstStepEventId, long maxLastEventId) {
    int start = Arrays.binarySearch(list, 0, numRecords, firstStepEventId);
    if (start < 0) {
      start = -start - 1;
    }
    int end = Arrays.binarySearch(list, 0, numRecords, maxLastEventId);
    if (end < 0) {
      end = -end - 1;
    }
    return new Iterator(list, start, end);
  }

  @Override
  public IdList.Iterator subListByOffset(int startOffset, int numIds) {
    return new Iterator(list, startOffset, startOffset + numIds);
  }

  @Override
  public Iterator iterator() {
    return new Iterator(list, 0, numRecords);
  }

  @Override
  public void close() throws IOException {
    File file = new File(filename);
    if (!file.exists()) {
      //noinspection ResultOfMethodCallIgnored
      file.getParentFile().mkdirs();
    }
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
      oos.writeObject(this);
    }
  }

  public static SimpleIdList build(String filename, int defaultCapacity) {
    File file = new File(filename);
    if (file.exists()) {
      try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
        return (SimpleIdList) ois.readObject();
      } catch (ClassNotFoundException | IOException e) {
        throw new RuntimeException(e);
      }
    }
    return new SimpleIdList(filename, new long[defaultCapacity], 0);
  }

  public static class Iterator implements IdList.Iterator {
    private final long[] list;
    private final int start;
    private final int end;
    private int offset;

    public Iterator(long[] list, int start, int end) {
      this.list = list;
      this.start = start;
      this.end = end;
      this.offset = 0;
    }

    @Override
    public boolean hasNext() {
      return start + offset < end;
    }

    @Override
    public long next() {
      return list[start + (offset++)];
    }
  }
}
