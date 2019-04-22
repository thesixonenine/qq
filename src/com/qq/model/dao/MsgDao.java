package com.qq.model.dao;

import com.qq.model.entity.Msg;
import com.qq.util.JdbcTemplate;
import com.qq.util.JdbcTemplate.PackEntity;
import com.qq.util.JdbcTemplate.PreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class MsgDao extends ImplCommonDao<Msg> {

    @Override
    public List<Msg> listAll() {
        return super.listAll();
    }

    @Override
    public List<Msg> listPart(final String... conditions) {
        synchronized (MsgDao.class) {
            if (conditions.length == 1) {
                String sql = "select * from qq_msg where qq_msg_receiver = ? order by qq_msg_date desc";
                return JdbcTemplate.query(sql, new PreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement pstmt) throws SQLException {
                        pstmt.setInt(1, Integer.parseInt(conditions[0]));
                    }
                }, createPack());
            } else if (conditions.length == 5) {
                String sql = "select * from (select rownum myrow,qq_msg.* from qq_msg where qq_msg_receiver in (?,?) and qq_user_id in (?,?) and qq_msg_type = ?  order by qq_msg_date ) where myrow between ? and ?  ";
                return JdbcTemplate.query(sql, new PreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement pstmt) throws SQLException {
                        pstmt.setInt(1, Integer.parseInt(conditions[0]));
                        pstmt.setInt(2, Integer.parseInt(conditions[1]));
                        pstmt.setInt(3, Integer.parseInt(conditions[0]));
                        pstmt.setInt(4, Integer.parseInt(conditions[1]));
                        pstmt.setInt(5, Integer.parseInt(conditions[2]));
                        pstmt.setInt(6, Integer.parseInt(conditions[3]));
                        pstmt.setInt(7, Integer.parseInt(conditions[4]));
                    }
                }, createPack());
            } else if (conditions.length == 4) {
                String sql = "select * from qq_msg where qq_msg_receiver = ? and qq_msg_type = ? and qq_msg_status = ? and qq_user_id = ? order by qq_msg_date";
                return JdbcTemplate.query(sql, new PreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement pstmt) throws SQLException {
                        pstmt.setInt(1, Integer.parseInt(conditions[0]));
                        pstmt.setInt(2, Integer.parseInt(conditions[1]));
                        pstmt.setInt(3, Integer.parseInt(conditions[2]));
                        pstmt.setInt(4, Integer.parseInt(conditions[3]));
                    }
                }, createPack());
            }
            return null;
        }
    }

    private PackEntity<Msg> createPack() {
        return new PackEntity<Msg>() {

            @Override
            public Msg packEntity(ResultSet rs) throws SQLException {
                Msg msg = new Msg();
                msg.setId(rs.getInt("qq_msg_id"));
                msg.setContent(rs.getString("qq_msg_content"));
                msg.setData(new Date(rs.getLong("qq_msg_date")));
                msg.setReceiver(rs.getInt("qq_msg_receiver"));
                msg.setStatus(rs.getInt("qq_msg_status"));
                msg.setType(rs.getInt("qq_msg_type"));
                msg.setUserId(rs.getInt("qq_user_id"));
                return msg;
            }
        };
    }

    @Override
    synchronized public Msg findById(final int id) {
        synchronized (MsgDao.class) {
            return JdbcTemplate.singleQuery("select * from qq_msg where qq_msg_id = ?", new PreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement pstmt) throws SQLException {
                    pstmt.setInt(1, id);
                }
            }, createPack());
        }
    }

    @Override
    public int update(final Msg t) {
        synchronized (MsgDao.class) {
            String sql = "insert into qq_msg(qq_user_id,qq_msg_content,qq_msg_date,qq_msg_receiver,qq_msg_status,qq_msg_type) values(?,?,?,?,?,?)";
            int flag = JdbcTemplate.update(sql, new PreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement pstmt) throws SQLException {
                    pstmt.setInt(1, t.getUserId());
                    pstmt.setString(2, t.getContent());
                    pstmt.setLong(3, System.currentTimeMillis());
                    pstmt.setInt(4, t.getReceiver());
                    pstmt.setInt(5, t.getStatus());
                    pstmt.setInt(6, t.getType());
                }
            });
            if (flag > 0) {
                sql = "select qq_msg_seq.currval result  from dual";
                Msg fg = (Msg) JdbcTemplate.singleQuery(sql, createPack1());
                return fg.getId();
            }
            return 0;
        }
    }

    private PackEntity<Msg> createPack1() {
        return new PackEntity<Msg>() {

            @Override
            public Msg packEntity(ResultSet rs) throws SQLException {
                Msg fg = new Msg();
                fg.setId(rs.getInt("result"));
                return fg;
            }
        };
    }

    @Override
    public int updateByCondition(final String... conditions) {
        synchronized (MsgDao.class) {
            return JdbcTemplate.update("update qq_msg set qq_msg_status= ? where qq_msg_id = ?", new PreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement pstmt) throws SQLException {
                    pstmt.setInt(1, Integer.valueOf(conditions[0]));
                    pstmt.setInt(2, Integer.valueOf(conditions[1]));
                }

            });
        }
    }

    @Override
    public int updateById(int id) {
        return super.updateById(id);
    }


}
