package me.samuki.remainder;

public class ActionTodo {
    private long id;
    private int active;
    private String name;
    private String type;
    private long amount;
    private String date;
    private int repeat;
    private String often;
    private long toBeDone;

    public ActionTodo(long id, int active, String name, String type, long amount, String date, int repeat, String often, long toBeDone) {
        this.id = id;
        this.active = active;
        this.name = name;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.repeat = repeat;
        this.often = often;
        this.toBeDone = toBeDone;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public String getOften() {
        return often;
    }

    public void setOften(String often) {
        this.often = often;
    }

    public long getToBeDone() {
        return toBeDone;
    }

    public void setToBeDone(long toBeDone) {
        this.toBeDone = toBeDone;
    }
}
