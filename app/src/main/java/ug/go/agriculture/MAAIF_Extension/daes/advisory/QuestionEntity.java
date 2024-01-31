package ug.go.agriculture.MAAIF_Extension.daes.advisory;
public class QuestionEntity {
    private String id;
    private String keyword;
    private String farmer_id;
    private String farmer;
    private String parish_id;
    private String telephone;
    private String body;
    private String enterprise_id;
    private String inquiry_source;
    private String created_at;
    private String updated_at;
    private int has_media;
    private String media_url;
    private String responses;
    private String sender;
    private String user_id;


    public QuestionEntity(String id,
                          String keyword, String farmer_id, String farmer, String parish_id, String telephone, String body, String enterprise_id, String inquiry_source, String created_at, String updated_at,
                          int has_media,String media_url,String responses, String sender, String user_id
    ) {
        this.id = id;
        this.keyword = keyword;
        this.farmer_id = farmer_id;
        this.farmer = farmer;
        this.parish_id = parish_id;
        this.telephone = telephone;
        this.body = body;
        this.enterprise_id = enterprise_id;
        this.inquiry_source = inquiry_source;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.has_media = has_media;
        this.media_url = media_url;
        this.responses = responses;
        this.user_id = user_id;


    }

    public String getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getEnterprise_id() {
        return enterprise_id;
    }

    public String getFarmer_id() {
        return farmer_id;
    }

    public String getInquiry_source() {
        return inquiry_source;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getParish_id() {
        return parish_id;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public int getHas_media() {
        return has_media;
    }

    public String getMedia_url() {
        return media_url;
    }

    public String getFarmer() {
        return farmer;
    }

    public String getResponses() {
        return responses;
    }

    public String getUser_id() {
        return user_id;
    }
}
