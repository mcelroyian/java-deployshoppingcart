package com.lambdaschool.shoppingcart.services;

import com.lambdaschool.shoppingcart.exceptions.ResourceFoundException;
import com.lambdaschool.shoppingcart.exceptions.ResourceNotFoundException;
import com.lambdaschool.shoppingcart.handlers.HelperFunctions;
import com.lambdaschool.shoppingcart.models.Role;
import com.lambdaschool.shoppingcart.models.User;
import com.lambdaschool.shoppingcart.models.UserRoles;
import com.lambdaschool.shoppingcart.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional
@Service(value = "userService")
public class UserServiceImpl
        implements UserService
{
    /**
     * Connects this service to the users repository
     */
    @Autowired
    private UserRepository userrepos;

    @Autowired
    private CartService cartService;

    @Autowired RoleService roleService;

    @Autowired UserAuditing userAuditing;

    @Autowired
    private HelperFunctions helper;

    @Override
    public List<User> findAll()
    {
        List<User> list = new ArrayList<>();
        /*
         * findAll returns an iterator set.
         * iterate over the iterator set and add each element to an array list.
         */
        userrepos.findAll()
                .iterator()
                .forEachRemaining(list::add);
        return list;
    }

    @Override
    public User findUserById(long id) throws ResourceNotFoundException
    {
        return userrepos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User id " + id + " not found!"));
    }

    @Transactional
    @Override
    public void delete(long id)
    {
        userrepos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User id " + id + " not found!"));
        userrepos.deleteById(id);
    }

    @Transactional
    @Override
    public User save(User user)
    {
        User newUser = new User();

        if (user.getUserid() != 0)
        {
            User oldUser = userrepos.findById(user.getUserid())
                    .orElseThrow(() -> new ResourceNotFoundException("User id " + user.getUserid() + " not found!"));

            // delete the roles for the old user we are replacing
            for (UserRoles ur : oldUser.getRoles())
            {
                deleteUserRole(ur.getUser()
                                .getUserid(),
                        ur.getRole()
                                .getRoleid());
            }
            newUser.setUserid(user.getUserid());
        }

        newUser.setUsername(user.getUsername());
        newUser.setPasswordNoEncrypt(user.getPassword());
        newUser.setComments(user.getComments());

        newUser.getRoles()
                .clear();
        if (user.getUserid() == 0)
        {
            for (UserRoles ur : user.getRoles())
            {
                Role newRole = roleService.findRoleById(ur.getRole()
                        .getRoleid());

                newUser.addRole(newRole);
            }
        } else
        {
            // add the new roles for the user we are replacing
            for (UserRoles ur : user.getRoles())
            {
                addUserRole(newUser.getUserid(),
                        ur.getRole()
                                .getRoleid());
            }
        }

        if (user.getCarts()
                .size() > 0)
        {
            throw new ResourceFoundException("Carts are not added through users");
        }
        return userrepos.save(newUser);
    }

    @Override
    public User findByName(String name) {
        User uu = userrepos.findByUsername(name.toLowerCase());
        if (uu == null)
        {
            throw new ResourceNotFoundException("User name " + name + " not found!");
        }
        return uu;
    }

    @Override
    public List<User> findByNameContaining(String username) {
        return userrepos.findByUsernameContainingIgnoreCase(username.toLowerCase());
    }

    @Override
    public User update(User user, long id) {
        return null;
    }

    @Override
    public void addUserRole(long userid, long roleid) {
        userrepos.findById(userid)
                .orElseThrow(() -> new ResourceNotFoundException("User id " + userid + " not found!"));
        roleService.findRoleById(roleid);

        if (userrepos.checkUserRolesCombo(userid,
                roleid)
                .getCount() <= 0)
        {
            userrepos.insertUserRoles(userAuditing.getCurrentAuditor()
                            .get(),
                    userid,
                    roleid);
        } else
        {
            throw new ResourceFoundException("Role and User Combination Already Exists");
        }
    }

    @Override
    public void deleteUserRole(long userid, long roleid) {
        userrepos.findById(userid)
                .orElseThrow(() -> new ResourceNotFoundException("User id " + userid + " not found!"));
        roleService.findRoleById(roleid);

        if (userrepos.checkUserRolesCombo(userid,
                roleid)
                .getCount() > 0)
        {
            userrepos.deleteUserRoles(userid,
                    roleid);
        } else
        {
            throw new ResourceNotFoundException("Role and User Combination Does Not Exists");
        }
    }
}