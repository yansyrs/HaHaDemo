package com.yan.haha.units;


public class BrainRiddle {

    private String id;
    private String question;
    private String answer;
    private boolean favorite;

    public BrainRiddle(String id, String question, String answer, boolean favorite) {
        super();
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.favorite = favorite;
    }

    public BrainRiddle(String id, String question, String answer) {
        this(id, question, answer, false);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean isFavorite() {
        return favorite;
    }
}
