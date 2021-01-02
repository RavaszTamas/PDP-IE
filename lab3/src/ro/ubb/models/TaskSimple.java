package ro.ubb.models;

public class TaskSimple {
    int threadCount;

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public int getRowSize() {
        return rowSize;
    }

    public void setRowSize(int rowSize) {
        this.rowSize = rowSize;
    }

    public int getCommonSize() {
        return commonSize;
    }

    public void setCommonSize(int commonSize) {
        this.commonSize = commonSize;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;
    }

    public TaskSimple(int threadCount, int rowSize, int commonSize, int columnSize) {
        this.threadCount = threadCount;
        this.rowSize = rowSize;
        this.commonSize = commonSize;
        this.columnSize = columnSize;
    }

    int rowSize,commonSize,columnSize;
}
