package com.yan.haha;

/**
 * Created by Leung on 2016/5/21.
 */
public class Horoscope {
    private String type;
    private String name;
    private String dateTime;
    private String allPoints;
    private String QFriend;
    private String color;
    private String health;
    private String love;
    private String money;
    private String luckyNum;
    private String summary;
    private String summaryTitle;
    private String work;
    private String job;
    private String weekth;
    private String luckyStone;
    private String year;

    //星座运程:today,tomorrow
    public Horoscope(String type, String name, String dateTime, String allPoints, String QFriend, String color,
                     String health, String love, String money, String luckyNum, String summary, String work) {
        super();
        this.type = type;
        this.name = name;
        this.dateTime = dateTime;
        this.allPoints = allPoints;
        this.QFriend = QFriend;
        this.color = color;
        this.health = health;
        this.love = love;
        this.money = money;
        this.luckyNum = luckyNum;
        this.summary = summary;
        this.work = work;
    }

    //星座运程:week, nextweek
    public Horoscope(String type, String name, String dateTime, String health, String job, String love,
                     String money, String weekth, String work) {
        super();
        this.type = type;
        this.name = name;
        this.dateTime = dateTime;
        this.health = health;
        this.job = job;
        this.love = love;
        this.money = money;
        this.weekth = weekth;
        this.work = work;
    }

    //星座运程:month
    public Horoscope(String type, String name, String dateTime, String allPoints, String health, String love,
                     String money, String work) {
        super();
        this.type = type;
        this.name = name;
        this.dateTime = dateTime;
        this.allPoints = allPoints;
        this.health = health;
        this.love = love;
        this.money = money;
        this.work = work;
    }

    //星座运程:year
    public Horoscope(String type, String name, String dateTime, String year, String summary, String summaryTitle,
                     String work, String love, String health, String money, String luckyStone) {
        super();
        this.type = type;
        this.name = name;
        this.dateTime = dateTime;
        this.year = year;
        this.summary = summary;
        this.summaryTitle = summaryTitle;
        this.work = work;
        this.love = love;
        this.health = health;
        this.money = money;
        this.luckyStone = luckyStone;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDateTime() {
        return dateTime;
    }
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getAllPoints() {
        return allPoints;
    }
    public void setAllPoints(String allPoints) {
        this.allPoints = allPoints;
    }

    public String getQFriend() {
        return QFriend;
    }
    public void setQFriend(String QFriend) {
        this.QFriend = QFriend;
    }

    public String getColor() {
        return color;
    }
    public void setColor(String body) {
        this.color = color;
    }

    public String getHealth() {
        return health;
    }
    public void setHealth(String body) {
        this.health = body;
    }

    public String getLove() {
        return love;
    }
    public void setLove(String body) {
        this.love = love;
    }

    public String getMoney() {
        return money;
    }
    public void setMoney(String money) {
        this.money = money;
    }

    public String getLuckyNum() {
        return luckyNum;
    }
    public void setLuckyNum(String luckyNum) {
        this.luckyNum = luckyNum;
    }

    public String getSummary() {
        return summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getWork() {
        return work;
    }
    public void setWork(String work) {
        this.work = work;
    }

    public String getJob() {
        return job;
    }
    public void setJob(String job) {
        this.job = job;
    }

    public String getSummaryTitle() {
        return summaryTitle;
    }
    public void setSummaryTitle(String summaryTitle) {
        this.summaryTitle = summaryTitle;
    }

    public String getWeekth() {
        return weekth;
    }
    public void setWeekth(String weekth) {
        this.weekth = weekth;
    }

    public String getLuckyStone() {
        return luckyStone;
    }
    public void setLuckyStone(String luckyStone) {
        this.luckyStone = luckyStone;
    }

    public String getYear() {
        return year;
    }
    public void setYear(String year) {
        this.year = year;
    }
}
