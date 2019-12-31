package springbook.user.service;

import org.springframework.mail.MailSender;
import springbook.user.domain.User;

public interface UserService {
    void add(User user);
    void upgradeLevels();
}
