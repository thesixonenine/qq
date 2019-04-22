package com.qq.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    public static interface PreparedStatementSetter {
        void setValues(PreparedStatement pstmt) throws SQLException;
    }

    public static interface PackEntity<T> {
        T packEntity(ResultSet rs) throws SQLException;
    }

    public static <T> List<T> query(String sql, PreparedStatementSetter setter, PackEntity<T> pack) {
        ResultSet rs = null;
        try {
            rs = query(sql, setter);
            List<T> list = new ArrayList<T>();
            if (pack != null) {
                while (rs.next()) {
                    list.add(pack.packEntity(rs));
                }
            }
            return list;
        } catch (Exception e) {
            throw new JdbcTemplateException("集合查询失败");
        } finally {
            DBUtils.release(rs);
        }
    }

    public static <T> T singleQuery(String sql, PreparedStatementSetter setter, PackEntity<T> pack) {
        ResultSet rs = null;
        try {
            rs = query(sql, setter);
            if (pack != null) {
                if (rs.next()) {
                    return pack.packEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new JdbcTemplateException("单个查询失败");
        } finally {
            DBUtils.release(rs);
        }
        return null;

    }

    public static ResultSet query(String sql, PreparedStatementSetter setter) throws SQLException {
        PreparedStatement pstmt = ConnectionManager.getConnection().prepareStatement(sql);
        if (setter != null) {
            setter.setValues(pstmt);
        }
        return pstmt.executeQuery();
    }

    public static int update(String sql, PreparedStatementSetter setter) {
        Connection conn = ConnectionManager.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            if (setter != null) {
                setter.setValues(pstmt);
            }
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcTemplateException(e);
        } finally {
            DBUtils.release(pstmt);
        }

    }

    public static int update(String sql) {
        return update(sql, null);
    }

    public static <T> List<T> query(String sql, PackEntity<T> pack) {
        return query(sql, null, pack);

    }

    public static <T> T singleQuery(String sql, PackEntity<T> pack) {
        return singleQuery(sql, null, pack);
    }

    public static class JdbcTemplateException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public JdbcTemplateException() {
            super();
        }

        public JdbcTemplateException(String message, Throwable cause,
                                     boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }

        public JdbcTemplateException(String message, Throwable cause) {
            super(message, cause);
        }

        public JdbcTemplateException(String message) {
            super(message);
        }

        public JdbcTemplateException(Throwable cause) {
            super(cause);
        }


    }

}