package springbook.user.domain;

public class User {
    String id;
    String name;
    String password;
    Level level; // enum
    int login;
    int recommend;
    String email;

    public User(String id,String name,String password,Level level,int login,int recommend,String email){
        this.id = id;
        this.name = name;
        this.password = password;
        this.level = level;
        this.login = login;
        this.recommend = recommend;
        this.email = email;
    }

    public User(){}
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public int getLogin() {
        return login;
    }

    public void setLogin(int login) {
        this.login = login;
    }

    public int getRecommend() {
        return recommend;
    }

    public void setRecommend(int recommend) {
        this.recommend = recommend;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void upgradeLevel(){
        Level nextLv = this.level.nextLevel();
        if(nextLv == null)
            throw new IllegalStateException(this.level+" is cannot be upgrade");
        else
            this.level = nextLv;
    }
}