package com.condition.umzzal.object;

public class Video {
    private int video_no; // 비디오 번호
    private String video_name; // 비디오 이름
    private String video_thumbnail; // 비디오 썸네일
    private String video_route; // 비디오 경로

    public Video() {
    }

    public Video(int video_no, String video_name, String video_thumbnail, String video_route) {
        this.video_no = video_no;
        this.video_name = video_name;
        this.video_thumbnail = video_thumbnail;
        this.video_route = video_route;
    }

    public void setVideo_no(int video_no) {
        this.video_no = video_no;
    }

    public void setVideo_name(String video_name) {
        this.video_name = video_name;
    }

    public void setVideo_thumbnail(String video_thumbnail) {
        this.video_thumbnail = video_thumbnail;
    }

    public void setVideo_route(String video_route) {
        this.video_route = video_route;
    }

    public int getVideo_no() {
        return video_no;
    }

    public String getVideo_name() {
        return video_name;
    }

    public String getVideo_thumbnail() {
        return video_thumbnail;
    }

    public String getVideo_route() {
        return video_route;
    }
} // Video class
