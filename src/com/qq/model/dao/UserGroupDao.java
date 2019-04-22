package com.qq.model.dao;

import com.qq.model.entity.UserGroup;
import com.qq.util.JdbcTemplate;
import com.qq.util.JdbcTemplate.PreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class UserGroupDao extends ImplCommonDao<UserGroup> {

    @Override
    public List<UserGroup> listAll() {
        // TODO Auto-generated method stub
        return super.listAll();
    }

    @Override
    public List<UserGroup> listPart(String... conditions) {
        // TODO Auto-generated method stub
        return super.listPart(conditions);
    }

    @Override
    public UserGroup findById(int id) {
        // TODO Auto-generated method stub
        return super.findById(id);
    }

    @Override
    public UserGroup findByConditions(String... conditions) {
        return super.findByConditions(conditions);
    }

    @Override
    public int update(UserGroup t) {
        // TODO Auto-generated method stub
        return super.update(t);
    }

    @Override
    public int updateByCondition(final String... conditions) {
        String sql = "insert into qq_user_group values(?,?)";
        return JdbcTemplate.update(sql, new PreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setInt(1, Integer.parseInt(conditions[0]));
                pstmt.setInt(2, Integer.parseInt(conditions[1]));
            }
        });
    }


}
