package hu.martin.ems.core.service;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.User;
import hu.martin.ems.core.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@NeedCleanCoding
public class UserService extends BaseService<User, UserRepository> {
    public UserService(UserRepository repo) { super(repo); }

    public User findByUsername(String userName) { return this.repo.findByUserName(userName); }
    public List<User> findAll(Boolean withDeleted) { return this.repo.customFindAll(withDeleted); }
    public User userExists(String username) { return this.repo.userExists(username);}
}
