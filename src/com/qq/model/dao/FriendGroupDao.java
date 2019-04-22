package com.qq.model.dao;

import com.qq.model.entity.FriendGroup;
import com.qq.util.JdbcTemplate;
import com.qq.util.JdbcTemplate.PackEntity;
import com.qq.util.JdbcTemplate.PreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FriendGroupDao extends ImplCommonDao<FriendGroup> {


    @Override
    public FriendGroup findById(final int id) {
        String sql = "select * from qq_friendGroup where qq_friendGroup_id = ?";
        return JdbcTemplate.singleQuery(sql, new PreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setInt(1, id);
            }
        }, createPack());
    }

    private PackEntity<FriendGroup> createPack() {
        return new PackEntity<FriendGroup>() {

            @Override
            public FriendGroup packEntity(ResultSet rs) throws SQLException {
                FriendGroup fg = new FriendGroup();
                fg.setId(rs.getInt("qq_friendGroup_id"));
                fg.setName(rs.getString("qq_friendGroup_name"));
                return fg;
            }
        };
    }

    @Override
    public FriendGroup findByConditions(final String... conditions) {
        return JdbcTemplate.singleQuery("select * from qq_friendGroup where qq_friendGroup_name = ?",
                new PreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement pstmt) throws SQLException {
                        pstmt.setString(1, conditions[0]);
                    }
                }, createPack());
    }

    @Override
    public int updateByCondition(final String... conditions) {
        String sql = "insert into qq_friendGroup(qq_friendGroup_name) values(?)";
        int flag = JdbcTemplate.update(sql, new PreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, conditions[0]);
            }
        });
        if (flag > 0) {
            sql = "select qq_friendGroup_seq.currval result  from dual";
            FriendGroup fg = (FriendGroup) JdbcTemplate.singleQuery(sql, createPack1());
            return fg.getId();
        }
        return 0;
    }

    private PackEntity<Object> createPack1() {
        return new PackEntity<Object>() {

            @Override
            public Object packEntity(ResultSet rs) throws SQLException {
                FriendGroup fg = new FriendGroup();
                fg.setId(rs.getInt("result"));
                return fg;
            }
        };
    }


}
