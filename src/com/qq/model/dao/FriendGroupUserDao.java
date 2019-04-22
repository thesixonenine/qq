package com.qq.model.dao;

import com.qq.model.entity.FriendGroupUser;
import com.qq.util.JdbcTemplate;
import com.qq.util.JdbcTemplate.PackEntity;
import com.qq.util.JdbcTemplate.PreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class FriendGroupUserDao extends ImplCommonDao<FriendGroupUser> {


    @Override
    public List<FriendGroupUser> listPart(final String... conditions) {
        if (conditions.length == 1) {
            String sql = "select * from friendGroup_user where qq_user_id = ?";
            return JdbcTemplate.query(sql, new PreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement pstmt) throws SQLException {
                    pstmt.setInt(1, Integer.parseInt(conditions[0]));
                }
            }, createPack());
        } else {
            String sql = "select * from friendGroup_user where qq_friendGroup_id = ?";
            return JdbcTemplate.query(sql, new PreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement pstmt) throws SQLException {
                    pstmt.setInt(1, Integer.parseInt(conditions[1]));
                }
            }, createPack());
        }

    }

    private PackEntity<FriendGroupUser> createPack() {
        return new PackEntity<FriendGroupUser>() {

            @Override
            public FriendGroupUser packEntity(ResultSet rs) throws SQLException {
                FriendGroupUser fgu = new FriendGroupUser();
                fgu.setFriendGroupId(rs.getInt("qq_friendGroup_id"));
                fgu.setUserId(rs.getInt("qq_user_id"));
                return fgu;
            }
        };
    }

    @Override
    public int updateByCondition(final String... conditions) {
        String sql = "insert into friendGroup_user values(?,?)";
        return JdbcTemplate.update(sql, new PreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setInt(1, Integer.parseInt(conditions[0]));
                pstmt.setInt(2, Integer.parseInt(conditions[1]));

            }
        });
    }

    @Override
    public int update(FriendGroupUser t) {
        return super.update(t);
    }


}
