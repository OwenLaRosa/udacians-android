package com.owenlarosa.udacians.data;

/**
 * Created by Owen LaRosa on 11/20/16.
 */

public class ProfileInfo {

    private String site;
    private String blog;
    private String linkedin;
    private String twitter;

    public ProfileInfo() {}

    public ProfileInfo(String site, String blog, String linkedin, String twitter) {
        this.site = site;
        this.blog = blog;
        this.linkedin = linkedin;
        this.twitter = twitter;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getBlog() {
        return blog;
    }

    public void setBlog(String blog) {
        this.blog = blog;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }
}
