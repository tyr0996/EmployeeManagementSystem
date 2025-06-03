package hu.martin.ems.core.service;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.User;
import hu.martin.ems.core.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@NeedCleanCoding
public class UserService extends BaseService<User, UserRepository> {
    public UserService(UserRepository repo) {
        super(repo);
    }

    public User findByUsername(String userName) {
        return this.repo.findByUserName(userName);
    }
}
