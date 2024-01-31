package ug.go.agriculture.MAAIF_Extension.farmer.advisory;

public class QuestionResponseEntity {
    private String id;
    private String user_name;
    private String user_role;
    private String response;
    private String created_at;
    private String user_id;
    private String question_id;


    public QuestionResponseEntity(String id,
                                  String user_id, String user_name, String user_role, String response, String created_at, String question_id
    ) {
        this.id = id;
        this.user_name = user_name;
        this.user_id = user_id;
        this.user_role = user_role;
        this.response = response;
        this.created_at = created_at;
        this.question_id = question_id;


    }

    public String getId() {
        return id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getResponse() {
        return response;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getUser_role() {
        return user_role;
    }

    public String getQuestion_id() {
        return question_id;
    }

    public String getCommentor(){
        return user_name+" ["+user_role+"]";
    }
}
