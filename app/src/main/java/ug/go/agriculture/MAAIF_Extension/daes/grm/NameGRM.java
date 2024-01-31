package ug.go.agriculture.MAAIF_Extension.daes.grm;

public class NameGRM {


    private String district;
    private String subcounty;
    private String parish;
    private String name;
    private String age;

    private String gender;

    private String phone;

    private String feedback;
    private String anonymmous;
    private String date_of_grievance;
    private String gNature;
    private String gType;
    private String gTypeNotListed;
    private String modeReceipt;
    private String description;
    private String past_actions;
    private String settle_otherwise;
    private String latitude;
    private String longitude;
    private String ref_number;
    private Integer synced;

    public NameGRM(String district, String subcounty, String parish, String name, String age,
                   String gender, String phone, String feedback, String anonymmous,
                   String date_of_grievance, String gNature, String gType, String gTypeNotListed, String modeReceipt,
                   String description, String past_actions,
                   String settle_otherwise, String latitude, String longitude, String ref_number, Integer synced) {
        this.district = district;
        this.subcounty = subcounty;
        this.parish = parish;
        this.name = name;
        this.age = age;
         this.gender = gender;
        this.phone = phone;
         this.feedback = feedback;
        this.anonymmous = anonymmous;
        this.date_of_grievance = date_of_grievance;
        this.gNature = gNature;
        this.gType = gType;
        this.gTypeNotListed = gTypeNotListed;
        this.modeReceipt = modeReceipt;
        this.description = description;
        this.past_actions= past_actions;
         this.settle_otherwise = settle_otherwise;
        this.latitude = latitude;
        this.longitude = longitude;
        this.ref_number = ref_number;
        this.synced = synced;
    }

    public String getDistrict() {
        return district;
    }
    public String getSubcounty() {
        return subcounty;
    }
    public String getParish() {
        return parish;
    }
    public String getName() { return name; }
    public String getAge() {
        return age;
    }
    public String getGender() {
        return gender;
    }
    public String getPhone() {
        return phone;
    }
    public String getFeedback() {
        return feedback;
    }
    public String getAnonymmous() {
        return anonymmous;
    }
    public String getDate_of_grievance() {
        return date_of_grievance;
    }
    public String getgNature() {
        return gNature;
    }
    public String getgType() {
        return gType;
    }
    public String getgTypeNotListed() {
        return gTypeNotListed;
    }
    public String getModeReceipt() {
        return modeReceipt;
    }
    public String getDescription() {
        return description;
    }
    public String getPast_actions() {
        return past_actions;
    }


    public String getSettle_otherwise() {
        return settle_otherwise;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getRef_number() {
        return ref_number;
    }

    public Integer getSynced() {
        return synced;
    }
}