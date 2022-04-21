package com.example.VisualTour;

public class CourseModel {

    private String course_name;
    private int course_rating;
    private int course_image;
    private String id;

    // Constructor
    public CourseModel(String course_name, int course_rating, int course_image) {
        this.course_name = course_name;
        this.course_rating = course_rating;
        this.course_image = course_image;

    }
    public CourseModel(String course_name, int course_rating, int course_image, String id) {
        this.course_name = course_name;
        this.course_rating = course_rating;
        this.course_image = course_image;
        this.id = id;
    }
    public void changeText1(String str){
        course_name=str;
    }
    // Getter and Setter
    public String getCourse_name() {
        return course_name;
    }
    public String getID(){return id;}
    public void setCourse_name(String course_name) {
        this.course_name = course_name;
    }

    public int getCourse_rating() {
        return course_rating;
    }

    public void setCourse_rating(int course_rating) {
        this.course_rating = course_rating;
    }

    public int getCourse_image() {
        return course_image;
    }
    public String getTitle() {
        return course_name;
    }
    public void setCourse_image(int course_image) {
        this.course_image = course_image;
    }
}
