package com.yan.haha.units;

/**
 * Created by Leung on 2016/5/21.
 */
public class Horoscope {
    private String type;
    private String name;
    private String dateTime;
    private String complexPoints;
    private String QFriend;
    private String color;
    private String health;
    private String healthPoints;
    private String love;
    private String lovePoints;
    private String money;
    private String moneyPoints;
    private int luckyNum;
    private String summary;
    private String summaryTitle;
    private String work;
    private String workPoints;
    private String job;
    private int weekth;
    private String luckyStone;

    public void set(String type, String name, String dateTime, String complexPoints,
                    String QFriend, String color, String health, String healthPoints,
                    String love, String lovePoints, String money, String moneyPoints,
                    int luckyNum, String summary, String summaryTitle, String work,
                    String workPoints, String job, int weekth, String luckyStone) {
        this.type = type.trim();
        this.name = name.trim();
        this.dateTime = dateTime.trim();
        this.complexPoints = complexPoints.trim();
        this.QFriend = QFriend.trim();
        this.color = color.trim();
        this.health = health.trim();
        this.healthPoints = healthPoints.trim();
        this.love = love.trim();
        this.lovePoints = lovePoints.trim();
        this.money = money.trim();
        this.moneyPoints = moneyPoints.trim();
        this.luckyNum = luckyNum;
        this.summary = summary.trim();
        this.summaryTitle = summaryTitle.trim();
        this.work = work.trim();
        this.workPoints = workPoints.trim();
        this.job = job.trim();
        this.weekth = weekth;
        this.luckyStone = luckyStone.trim();
    }

    /** 星座运程:today,tomorrow
     *   type:查询类型
     *   name:星座名称
     *   dataTime:日期
     *   complexPoints:综合指数
     *   QFriend:速配星座
     *   color:幸运色
     *   healthPoints:健康指数
     *   lovePoints:爱情指数
     *   moneyPoints:金钱指数?
     *   luckyNum:幸运数字
     *   summary:综合评价
     *   workPoints:工作指数
     */
    public Horoscope(String type, String name, String dateTime, String complexPoints, String QFriend, String color,
                     String healthPoints, String lovePoints, String moneyPoints, int luckyNum, String summary, String workPoints) {
        super();
        set(type, name, dateTime, complexPoints, QFriend, color, "", healthPoints, "",
                lovePoints, "", moneyPoints, luckyNum, summary, "", "", workPoints, "", -1, "");
    }


    /** 星座运程:week, nextweek
     *   type:查询类型
     *   name:星座名称
     *   dataTime:日期
     *   health:健康总结
     *   job:求职心得
     *   love:恋情分析
     *   money:财运分析
     *   weekth:全年第几周
     *   work:工作指南
     */
    public Horoscope(String type, String name, String dateTime, String health, String job, String love,
                     String money, int weekth, String work) {
        super();
        set(type, name, dateTime, "", "", "", health, "", love, "",
                money, "", -1, "", "", work, "", job, weekth, "");
    }


    /** 星座运程:month
     *   type:查询类型
     *   name:星座名称
     *   dataTime:日期
     *   summary:综合评价
     *   health:健康总结
     *   love:恋情分析
     *   money:财运分析
     *   work:工作指南
     */
    public Horoscope(String type, String name, String dateTime, String summary, String health, String love,
                     String money, String work) {
        super();
        set(type, name, dateTime, "", "", "", health, "", love, "",
                money, "", -1, summary, "", work, "", "", -1, "");
    }


    /** 星座运程:year
     *   type:查询类型
     *   name:星座名称
     *   dataTime:日期
     *   summary:综合评价
     *   summaryTitle:综合评价标题
     *   work:工作指南
     *   love:恋情分析
     *   health:健康总结
     *   money:财运分析
     *   luckyStone:幸运石
     */
    public Horoscope(String type, String name, String dateTime, String summary, String summaryTitle,
                     String work, String love, String health, String money, String luckyStone) {
        super();
        set(type, name, dateTime, "", "", "", health, "", love, "", money, "",
                -1, summary, summaryTitle, work, "", "", -1, luckyStone);
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

    public String getcomplexPoints() {
        return complexPoints;
    }
    public void setcomplexPoints(String complexPoints) {
        this.complexPoints = complexPoints;
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

    public int getLuckyNum() {
        return luckyNum;
    }
    public void setLuckyNum(int luckyNum) {
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

    public int getWeekth() {
        return weekth;
    }
    public void setWeekth(int weekth) {
        this.weekth = weekth;
    }

    public String getLuckyStone() {
        return luckyStone;
    }
    public void setLuckyStone(String luckyStone) {
        this.luckyStone = luckyStone;
    }

    public String getHealthPoints() {
        return healthPoints;
    }
    public void setHealthPoints(String healthPoints) {
        this.healthPoints = healthPoints;
    }

    public String getLovePoints() {
        return lovePoints;
    }
    public void setLovePoints(String lovePoints) {
        this.lovePoints = lovePoints;
    }

    public String getMoneyPoints() {
        return moneyPoints;
    }
    public void setMoneyPoints(String moneyPoints) {
        this.moneyPoints = moneyPoints;
    }

    public String getWorkPoints() {
        return workPoints;
    }
    public void setWorkPoints(String workPoints) {
        this.workPoints = workPoints;
    }
}
