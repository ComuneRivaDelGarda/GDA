package it.tn.rivadelgarda.comune.suite;

import com.axiastudio.suite.base.ICheckLogin;

import java.sql.*;

/**
 * User: tiziano
 * Date: 22/10/13
 * Time: 14:45
 */
public class CheckPGUser implements ICheckLogin {

    private String url=null;

    @Override
    public Boolean check(String user, String password){
        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            return true;
        } catch (SQLException e) {
            return false;
        }

    }

    public void setJdbcUrl(String url){
        this.url = url;
    }

}
