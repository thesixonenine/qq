package com.qq.model.dao;

import com.qq.model.entity.User;
import com.qq.model.service.ServiceFactory;
import com.qq.util.JdbcTemplate;
import com.qq.util.JdbcTemplate.PackEntity;
import com.qq.util.JdbcTemplate.PreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao extends ImplCommonDao<User> {

    @Override
    public List<User> listPart(final String... conditions) {
        if (conditions.length == 1) {
            return super.listPart(conditions);
        } else if (conditions.length == 3) {
            String sql = "select * from qq_user where qq_user_name like ? or qq_user_account like ?  and qq_user_account != ?	order by qq_user_date desc  ";
            return JdbcTemplate.query(sql, new PreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement pstmt)
                        throws SQLException {
                    pstmt.setString(1, "%" + conditions[0] + "%");
                    pstmt.setString(2, "%" + conditions[1] + "%");
                    pstmt.setString(3, conditions[2]);
                }

            }, createPack());
        } else if (conditions.length == 4) {
            String sql = "select * from qq_user where qq_user_id in (select qq_user_id from friendGroup_user where qq_friendGroup_id in (select q2.qq_friendgroup_id from qq_msg q1,qq_friendGroup q2,friendGroup_user q3 where "
                    + " q1.qq_msg_id=q3.qq_user_id and q2.qq_friendgroup_id=q3.qq_friendgroup_id " +
                    "and q1.qq_user_id = ? and q2.qq_friendgroup_name like ? ) and qq_user_id != ? )  and  qq_user_account = ? or qq_user_name  = ? ";
            return JdbcTemplate.query(sql, new PreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement pstmt)
                        throws SQLException {
                    pstmt.setInt(1, Integer.parseInt(conditions[0]));
                    pstmt.setString(2, "%" + conditions[1] + "%");
                    pstmt.setInt(3, Integer.parseInt(conditions[0]));
                    pstmt.setString(4, conditions[2]);
                    pstmt.setString(5, conditions[3]);
                }

            }, createPack());
        } else if (conditions.length == 5) {
            String sql = "select * from qq_user where qq_user_id in (select qq_user_id from friendGroup_user where qq_friendGroup_id in (select q2.qq_friendgroup_id from qq_msg q1,qq_friendGroup q2,friendGroup_user q3 where "
                    + " q1.qq_msg_id=q3.qq_user_id and q2.qq_friendgroup_id=q3.qq_friendgroup_id " +
                    "and q1.qq_user_id = ? and q2.qq_friendgroup_name like ? ) and qq_user_id != ? )  and  qq_user_account like ? or qq_user_name  like ? ";
            return JdbcTemplate.query(sql, new PreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement pstmt)
                        throws SQLException {
                    pstmt.setInt(1, Integer.parseInt(conditions[0]));
                    pstmt.setString(2, "%" + conditions[1] + "%");
                    pstmt.setInt(3, Integer.parseInt(conditions[0]));
                    pstmt.setString(4, "%" + conditions[2] + "%");
                    pstmt.setString(5, "%" + conditions[3] + "%");
                }

            }, createPack());
        }
        return null;
    }

    @Override
    public User findById(final int id) {
        return JdbcTemplate.singleQuery("select * from qq_user where qq_user_id = ?", new PreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setInt(1, id);
            }

        }, createPack());
    }

    @Override
    public User findByConditions(final String... conditions) {
        return JdbcTemplate.singleQuery("select * from qq_user where qq_user_account = ?",
                new PreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement pstmt) throws SQLException {
                        pstmt.setString(1, conditions[0]);
                    }
                }, createPack());

    }


    private PackEntity<User> createPack() {
        return new PackEntity<User>() {

            @Override
            public User packEntity(ResultSet rs) throws SQLException {
                User user = new User();
                user.setId(rs.getInt("qq_user_id"));
                user.setAccount(rs.getString("qq_user_account"));
                user.setPassword(rs.getString("qq_user_password"));
                user.setName(rs.getString("qq_user_name"));
                user.setSex(rs.getString("qq_user_sex"));
                user.setIcon(rs.getString("qq_user_icon"));
                user.setAll(rs.getInt("qq_user_allRights"));
                user.setLabel(rs.getString("qq_user_label"));
                user.setLevel(rs.getInt("qq_user_level"));
                user.setLogin(rs.getInt("qq_user_login"));
                user.setSendText(rs.getInt("qq_user_sendText"));
                user.setSendMsg(rs.getInt("qq_user_sendMsg"));
                user.setType(rs.getInt("qq_user_type"));
                return user;
            }
        };
    }

    @Override
    public int update(final User t) {

        try {
            String sql = "insert into qq_user(qq_user_account,qq_user_password) values(?,?)";
            int flag = JdbcTemplate.update(sql, new PreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement pstmt) throws SQLException {
                    pstmt.setString(1, t.getAccount());
                    pstmt.setString(2, t.getPassword());
                }
            });
            if (flag > 0) {
                System.out.println("我的好友(" + t.getAccount() + ")");
                int friendGroupId = ServiceFactory.getService("friendGroup").updateByConditions("我的好友 " + t.getAccount());
                User user = (User) ServiceFactory.getService("user").findByCondition(t.getAccount());
                String content = String.valueOf(friendGroupId) + " " + user.getId();
                int flag3 = DaoFactory.getDao("friendGroupUser").updateByCondition(content.split(" "));
//					return flag3;

//					int addGroup = ServiceFactory.getService("group").updateByConditions("QQ官方群");
//					if(addGroup>0){
                int addUserGroup = ServiceFactory.getService("userGroup").updateByConditions("" + user.getId(), "" + 4);
                if (addUserGroup > 0) {
                    System.out.println("添加群组成功!");
                }
                return addUserGroup;
//					}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;

    }

}
