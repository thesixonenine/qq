package com.qq.model.dao;

import com.qq.model.entity.Group;
import com.qq.util.JdbcTemplate;
import com.qq.util.JdbcTemplate.PackEntity;
import com.qq.util.JdbcTemplate.PreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class GroupDao extends ImplCommonDao<Group> {

    @Override
    public List<Group> listPart(final String... conditions) {
        String sql = "select * from qq_group where qq_group_id in (select q3.qq_group_id from qq_user q1,qq_user_group q3 where q1.qq_user_id=q3.qq_user_id  and q1.qq_user_id = ? )";
        return JdbcTemplate.query(sql, new PreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setInt(1, Integer.parseInt(conditions[0]));
            }
        }, createPack());
    }

    @Override
    public Group findById(int id) {
        return super.findById(id);
    }

    @Override
    public Group findByConditions(final String... conditions) {
        String sql = "select * from qq_group where qq_group_account = ?";
        return JdbcTemplate.singleQuery(sql, new PreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, conditions[0]);
            }
        }, createPack());
    }

    @Override
    public int updateByCondition(final String... conditions) {
        String sql = "insert into qq_group(qq_group_account) values(?)";
        int flag = JdbcTemplate.update(sql, new PreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, conditions[0]);
            }
        });
        if (flag > 0) {
            String sql1 = "select qq_group_seq.currval result  from dual";
            Group group = JdbcTemplate.singleQuery(sql1, new PackEntity<Group>() {

                @Override
                public Group packEntity(ResultSet rs) throws SQLException {
                    Group group = new Group();
                    group.setId(rs.getInt("result"));
                    return group;
                }
            });
            return group.getId();
        }
        return 0;

    }

    private PackEntity<Group> createPack() {
        return new PackEntity<Group>() {
            @Override
            public Group packEntity(ResultSet rs) throws SQLException {
                Group group = new Group();
                group.setAccount(rs.getString("qq_group_account"));
                group.setId(rs.getInt("qq_group_id"));
                group.setIcon(rs.getString("qq_group_icon"));
                group.setLabel(rs.getString("qq_group_label"));
                group.setLevel(rs.getInt("qq_group_level"));
                group.setName(rs.getString("qq_group_name"));
                group.setStatus(rs.getInt("qq_group_status"));
                return group;
            }
        };
    }


}
