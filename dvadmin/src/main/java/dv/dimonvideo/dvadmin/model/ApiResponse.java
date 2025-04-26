package dv.dimonvideo.dvadmin.model;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {
    @SerializedName("today")
    private String today;

    @SerializedName("uploader")
    private String uploader;

    @SerializedName("vuploader")
    private String vuploader;

    @SerializedName("muzon")
    private String muzon;

    @SerializedName("usernews")
    private String usernews;

    @SerializedName("gallery")
    private String gallery;

    @SerializedName("devices")
    private String devices;

    @SerializedName("forum")
    private String forum;

    @SerializedName("abuse_file")
    private String abuseFile;

    @SerializedName("abuse_forum")
    private String abuseForum;

    @SerializedName("space")
    private String space;

    @SerializedName("visitors")
    private String visitors;

    @SerializedName("tic")
    private String tic;

    @SerializedName("date")
    private String date;

    // Геттеры
    public String getToday() { return today; }
    public String getUploader() { return uploader; }
    public String getVuploader() { return vuploader; }
    public String getMuzon() { return muzon; }
    public String getUsernews() { return usernews; }
    public String getGallery() { return gallery; }
    public String getDevices() { return devices; }
    public String getForum() { return forum; }
    public String getAbuseFile() { return abuseFile; }
    public String getAbuseForum() { return abuseForum; }
    public String getSpace() { return space; }
    public String getVisitors() { return visitors; }
    public String getTic() { return tic; }
    public String getDate() { return date; }
}