package com.qq.util;

import java.sql.SQLException;

public class TransactionManager {

    public static void begin() {
        try {
            ConnectionManager.getConnection().setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.release();
        }
    }

    public static void rollback() {
        try {
            ConnectionManager.getConnection().rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.release();
        }
    }

    public static void commit() {
        try {
            ConnectionManager.getConnection().commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.release();
        }
    }
}
