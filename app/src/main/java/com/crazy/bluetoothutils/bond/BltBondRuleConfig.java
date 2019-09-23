package com.crazy.bluetoothutils.bond;

/**
 * 配对规则配置类
 * Created by Crazy on 2018/7/27.
 */

public class BltBondRuleConfig {

    //配对密码
    public String[] bluePairPasswords = {"0000", "1234"};

    //是否自动配对
    private boolean isAutoPaired = false;

    //配对密码
    private String pairPassword;

    public String[] getBluePairPasswords() {
        return bluePairPasswords;
    }

    public void setBluePairPasswords(String[] bluePairPasswords) {
        this.bluePairPasswords = bluePairPasswords;
    }

    public boolean isAutoPaired() {
        return isAutoPaired;
    }

    public void setAutoPaired(boolean autoPaired) {
        isAutoPaired = autoPaired;
    }

    public String getPairPassword() {
        return pairPassword;
    }

    public void setPairPassword(String pairPassword) {
        this.pairPassword = pairPassword;
    }
}
