package com.example.sharedailycard.model;

import java.io.Serializable;
// Serializable(직렬화) => Intent로 화면 간 전달할 때 putExtra로 전달하기 위함
public class DailyLog implements Serializable {
    private String date;
    private String imageUri; // 이미지 URI 경로 (String으로 저장)
    private float weight;
    private float bodyFat;
    private String workout;

    private String breakfast;
    private int kcalBreakfast;
    private String lunch;
    private int kcalLunch;
    private String dinner;
    private int kcalDinner;
    
    // 생성자 만들기
    public DailyLog(String date, String imageUri, float weight, float bodyFat, String workout,
                    String breakfast, int kcalBreakfast, String lunch, int kcalLunch,
                    String dinner, int kcalDinner) {
        this.date = date;
        this.imageUri = imageUri;
        this.weight = weight;
        this.bodyFat = bodyFat;
        this.workout = workout;
        this.breakfast = breakfast;
        this.kcalBreakfast = kcalBreakfast;
        this.lunch = lunch;
        this.kcalLunch = kcalLunch;
        this.dinner = dinner;
        this.kcalDinner = kcalDinner;
    }

    // 빈 생성자 (Firebase 사용 대비)
    public DailyLog() {}

    // Getter와 Setter
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }

    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }

    public float getBodyFat() { return bodyFat; }
    public void setBodyFat(float bodyFat) { this.bodyFat = bodyFat; }

    public String getWorkout() { return workout; }
    public void setWorkout(String workout) { this.workout = workout; }

    public String getBreakfast() { return breakfast; }
    public void setBreakfast(String breakfast) { this.breakfast = breakfast; }

    public int getKcalBreakfast() { return kcalBreakfast; }
    public void setKcalBreakfast(int kcalBreakfast) { this.kcalBreakfast = kcalBreakfast; }

    public String getLunch() { return lunch; }
    public void setLunch(String lunch) { this.lunch = lunch; }

    public int getKcalLunch() { return kcalLunch; }
    public void setKcalLunch(int kcalLunch) { this.kcalLunch = kcalLunch; }

    public String getDinner() { return dinner; }
    public void setDinner(String dinner) { this.dinner = dinner; }

    public int getKcalDinner() { return kcalDinner; }
    public void setKcalDinner(int kcalDinner) { this.kcalDinner = kcalDinner; }
}
