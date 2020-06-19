package com.lambdaschool.shoppingcart.repositories;

import com.lambdaschool.shoppingcart.models.User;
import com.lambdaschool.shoppingcart.views.JustTheCount;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserRepository
        extends CrudRepository<User, Long>
{

    User findByUsername(String username);

    List<User> findByUsernameContainingIgnoreCase(String name);


    @Query(value = "SELECT COUNT(*) as count FROM userroles WHERE userid = :userid AND roleid = :roleid",
            nativeQuery = true)
    JustTheCount checkUserRolesCombo(
            long userid,
            long roleid);


    /**
     * Deletes the given user, role combination
     *
     * @param userid The user id of the user of this user role combination
     * @param roleid The role id of the role of this user role combination
     */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM UserRoles WHERE userid = :userid AND roleid = :roleid")
    void deleteUserRoles(
            long userid,
            long roleid);



    /**
     * Inserts the new user role combination
     *
     * @param uname  The username (String) of the user adding the record
     * @param userid The user id of the user of this user role combination
     * @param roleid The role id of the role of this user role combination
     */
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO userroles(userid, roleid, created_by, created_date, last_modified_by, last_modified_date) VALUES (:userid, :roleid, :uname, CURRENT_TIMESTAMP, :uname, CURRENT_TIMESTAMP)",
            nativeQuery = true)
    void insertUserRoles(
            String uname,
            long userid,
            long roleid);
}
