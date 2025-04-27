package in.rjha.instagramclone.Model;

public class UploadPost {
    public String id;
    public String desc;
    public String email;

    public  String url;
    private String date; // date 추가
    public UploadPost() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public UploadPost(String id, String desc,String email,String url, String date) {
        this.id = id;
        this.desc = desc;
        this.email=email;
        this.url=url;
        this.date=date;
    }

    // getter, setter(for date) 추가
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
