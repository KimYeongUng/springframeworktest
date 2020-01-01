package springbook.service;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

import java.util.List;

public class UserServiceImpl implements UserService {

    UserDao userdao;
    public static final int MIN_LOGIN_FOR_SILVER = 50;
    public static final int MIN_RECOMMEND_FOR_GOLD = 30;
    private MailSender mailSender;

    public void setMailSender(MailSender mailSender){
        this.mailSender = mailSender;
    }

    public void setUserDao(UserDao dao){
        this.userdao = dao;
    }

    public void upgradeLevels(){
        List<User> users= userdao.getAll();
        for(User user:users){
            if(canUpgradeLevel(user))
                upgradeLevel(user);
        }
    }

    public void add(User user){
        if(user.getLevel() == null)
            user.setLevel(Level.BASIC);
        userdao.add(user);
    }

    public boolean canUpgradeLevel(User user){
        Level currentlv = user.getLevel();
        switch (currentlv){
            case BASIC: return (user.getLogin()>=MIN_LOGIN_FOR_SILVER);
            case SILVER: return (user.getRecommend()>=MIN_RECOMMEND_FOR_GOLD);
            case GOLD: return false;
            default: throw new IllegalArgumentException("unknown level: "+currentlv);
        }
    }

    public void upgradeLevel(User user){
        user.upgradeLevel();
        userdao.update(user);
        sendUpgradeEMail(user);
    }

    private void sendUpgradeEMail(User user){

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setFrom("useradmin@rover.com");
        mailMessage.setSubject("Upgrade Info.");
        mailMessage.setText("Your Grade is Upgraded :"+user.getLevel().name());

        this.mailSender.send(mailMessage);
    }

}
