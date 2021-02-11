package com.sixthday.navigation;

public class TestAccount {
    public static void main(String[] args) {
        String input = "<add-item item-descriptor=\"user\" id=\"C_USER_ID\">\n" +
                "  <set-property name=\"homeAddress\"><![CDATA[t1616870082]]></set-property>\n" +
                "  <set-property name=\"firstName\"><![CDATA[Ron01]]></set-property>\n" +
                "  <set-property name=\"login\"><![CDATA[C_EMAIL_U]]></set-property>\n" +
                "  <set-property name=\"email\"><![CDATA[C_EMAIL_L]]></set-property>\n" +
                "  <set-property name=\"lastName\"><![CDATA[Ronny01]]></set-property>\n" +
                "  <set-property name=\"webId\"><![CDATA[C_USER_ID]]></set-property>\n" +
                "  <set-property name=\"password\"><![CDATA[{AES-06-07-W}Y86G1NIlb5TkEya42EAiVg==]]></set-property>\n" +
                "  <set-property name=\"requestLocale\"><![CDATA[en_US]]></set-property>\n" +
                "  <set-property name=\"transfer\"><![CDATA[1]]></set-property>\n" +
                "  <set-property name=\"lifetimeOrderTotal\"><![CDATA[421.09]]></set-property>\n" +
                "  <set-property name=\"countryPreference\"><![CDATA[US]]></set-property>\n" +
                "  <set-property name=\"registrationDate\"><![CDATA[5/10/2019 11:23:31]]></set-property>\n" +
                "  <set-property name=\"pwd_archived\"><![CDATA[0]]></set-property>\n" +
                "  <set-property name=\"flgRegistered\"><![CDATA[true]]></set-property>\n" +
                "  <set-property name=\"last_pwd_update\"><![CDATA[5/10/2019 11:23:31]]></set-property>\n" +
                "  <set-property name=\"languageAlternative\"><![CDATA[en]]></set-property>\n" +
                "  <set-property name=\"creditCards\"><![CDATA[defaultCreditCard=t1usercc383580003]]></set-property>\n" +
                "  <set-property name=\"profileGroup\"><![CDATA[1]]></set-property>\n" +
                "  <set-property name=\"billingAddress\"><![CDATA[t1616870083]]></set-property>\n" +
                "  <set-property name=\"profileCreateDate\"><![CDATA[5/10/2019 11:17:02]]></set-property>\n" +
                "  <set-property name=\"passwordKeyDerivationFunction\"><![CDATA[8]]></set-property>\n" +
                "  <set-property name=\"lastActivity\"><![CDATA[5/10/2019 11:31:39]]></set-property>\n" +
                "  <set-property name=\"totalOrderTotalThisCalendarYear\"><![CDATA[421.09]]></set-property>\n" +
                "  <set-property name=\"numberOfOrders\"><![CDATA[1]]></set-property>\n" +
                "  <set-property name=\"receiveEmail\"><![CDATA[yes]]></set-property>\n" +
                "  <set-property name=\"invalidLoginAttempts\"><![CDATA[0]]></set-property>\n" +
                "  <set-property name=\"type\"><![CDATA[registered]]></set-property>\n" +
                "  <set-property name=\"lastPasswordUpdate\"><![CDATA[5/10/2019 11:17:02]]></set-property>\n" +
                "  <set-property name=\"purchaseDepictionCodes\"><![CDATA[H]]></set-property>\n" +
                "  <set-property name=\"shippingAddress\"><![CDATA[t1616870084]]></set-property>\n" +
                "  <set-property name=\"totalVisits\"><![CDATA[1]]></set-property>\n" +
                "  <set-property name=\"lastPurchaseState\"><![CDATA[TX]]></set-property>\n" +
                "  <set-property name=\"firstPurchaseDate\"><![CDATA[5/10/2019 11:19:28]]></set-property>\n" +
                "  <set-property name=\"lastOrderTotal\"><![CDATA[421.09]]></set-property>\n" +
                "  <set-property name=\"lastPurchase\"><![CDATA[5/10/2019 11:19:28]]></set-property>\n" +
                "  <set-property name=\"wishlist\"><![CDATA[t1gl604200027]]></set-property>\n" +
                "  <set-property name=\"currencyPreference\"><![CDATA[USD]]></set-property>\n" +
                "  <set-property name=\"languagePreference\"><![CDATA[en]]></set-property>\n" +
                "  <set-property name=\"defaultCreditCard\"><![CDATA[t1usercc383580003]]></set-property>\n" +
                "</add-item>";
        String email = "HASHTESTING";
        String emailPost = "@nmtest.info";
        int userId = 1708910026;
        for (int i = 0; i < 100; i++) {
            System.out.println(
                    input
                            .replace("C_USER_ID", ("t" + (userId + i)))
                            .replace("C_EMAIL_U", (email + (i + 2) + emailPost).toUpperCase())
                            .replace("C_EMAIL_L", (email + (i + 2) + emailPost).toLowerCase()));
        }
    }
}
