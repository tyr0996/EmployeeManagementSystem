package hu.martin.ems.core.service;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.model.User;
import hu.martin.ems.core.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@NeedCleanCoding
public class UserService extends BaseService<User, UserRepository> {
    public UserService(UserRepository repo) { super(repo); }

    public User findByUsername(String userName) { return this.repo.findByUserName(userName); }
    public List<User> findAll(Boolean withDeleted) { return this.repo.customFindAll(withDeleted); }
}
