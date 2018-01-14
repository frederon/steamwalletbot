package com.frederon;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.awt.datatransfer.*;
import java.awt.Toolkit;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Main {

    private static ArrayList<String> failedWallet = new ArrayList<>();
    private static ArrayList<String> successWallet = new ArrayList<>();
    private static Console console = System.console();

    private static String printFailedWallet() {
        StringJoiner joiner = new StringJoiner(", ");
        for(String s: failedWallet) {
            joiner.add(s);
        }
        return joiner.toString();
    }

    private static int findWalletIndex(String walletid, ArrayList<Wallet> wallets) {
        for(int i = 0; i < wallets.size(); i++ ) {
            if(walletid.equals(wallets.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    private static String loadFromFile(String location) throws IOException{
        try(BufferedReader br = new BufferedReader(new FileReader(location))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String everything = sb.toString();
            System.out.println("Successfully loaded file.");
            return everything;
        }
    }

    private static void wait(int second) {
        try
        {
            Thread.sleep((second*1000));
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }

    private static void setWallet(int id, ArrayList<Wallet> wallets) {
        if(id < 0) {
            System.out.println("Invalid input (Wrong id).");
            return;
        }
        String toCopy = wallets.get(id).getCode();
        StringSelection stringSelection = new StringSelection(toCopy);
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);
        System.out.println("CODE NUMBER " + (wallets.get(id).getId()));
    }

    private static ArrayList<Wallet> getWallet() throws IOException {
        System.out.println("Please enter your steam wallet code (.txt) location: ");
        Scanner location = new Scanner(System.in);
        String code = loadFromFile(location.nextLine());
        ArrayList<Wallet> wallets = new ArrayList<Wallet>();

        String[] array = code.split("\\s");
        ArrayList<String> wallet = new ArrayList<>(Arrays.asList(array));
        for (int i = 0; i < wallet.size(); i = i + 3) {
            Wallet toAdd = new Wallet(wallet.get(i), wallet.get(i + 1));
            wallets.add(toAdd);
        }
        return wallets;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Steam Wallet Activator Bot, Created by Frederic Ronaldi \n" +
                "===============================================================================");
        if (console == null) {
            System.out.println("Couldn't get Console instance");
            System.exit(0);
        }
        startRedeem();
    }

    private static boolean checkLogin(WebDriver driver) throws Exception {
        if(driver.findElements(By.id("wallet_code")).size() != 0){
            System.out.println("Logged in");
            return true;
        }else{
            System.out.println("Failed to logged in, please login manually. After logged in, type \"login\"");
            return false;
        }
    }

    private static boolean checkStatus(WebDriverWait driverWait) throws TimeoutException {
        try {
            return driverWait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("redeem_wallet_success_description"), "You have added Rp 6 000 to your Steam Wallet."));
        } catch(TimeoutException ex){
            return false;
        }
    }

    private static void startRedeem() throws Exception{
        System.setProperty("webdriver.chrome.driver", "D:\\Java\\selenium\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        Actions builder = new Actions(driver);
        Scanner s = new Scanner(System.in);
        Actions seriesOfActions;
        boolean loggedIn, suc;
        WebDriverWait driverWait = new WebDriverWait(driver,30);
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        driver.get("https://store.steampowered.com/login?redir=account%2Fredeemwalletcode");

        System.out.println("===============================================================================");
        WebElement username= driver.findElement(By.id("input_username"));
        System.out.println("Please enter your steam username :");

        seriesOfActions = builder.moveToElement(username).click().sendKeys(username, s.nextLine());
        seriesOfActions.perform();
        WebElement pass = driver.findElement(By.id("input_password"));
        String password = new String(console.readPassword("Please enter your steam password :\n"));
        Actions seriesOfAction = builder.moveToElement(pass).click().sendKeys(pass, password).sendKeys(Keys.ENTER);
        seriesOfAction.perform();

        System.out.println("PLEASE DO NOT CONTINUE UNTIL STEAM GUARD WINDOW POPS UP!");
        System.out.println("Please enter your Steam Guard code: ");
        WebElement guard = driver.findElement(By.id("twofactorcode_entry"));
        seriesOfActions = builder.moveToElement(guard).click().sendKeys(guard, s.nextLine()).sendKeys(Keys.ENTER);
        seriesOfActions.perform();

        System.out.println("Waiting 10 seconds to login. Please be patience..");
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.id("wallet_code")));

        driver.get("https://store.steampowered.com/account/redeemwalletcode");
        loggedIn = checkLogin(driver);
        WebElement swc;

        while(true) {
            if(loggedIn) {
                ArrayList<Wallet> wallets = getWallet();
                System.out.println("Steam Wallet Redeem from id :");
                int startid = findWalletIndex(s.nextLine(), wallets);
                System.out.println("Steam Wallet Redeem end to id : (Max.25/hour)");
                int endid = findWalletIndex(s.nextLine(), wallets);

                if(startid < 0 || endid < 0 || (endid-startid+1) > 25) {
                    System.out.println("Wrong steam wallet id or you exceeded maximal 25 steam wallet Redeem/hour.");
                    System.out.println("Exiting bot in 10 seconds");
                    wait(10);
                    System.exit(0);
                }

                Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
                String code;
                WebElement submit;
                while (startid <= endid) {
                    setWallet(startid, wallets);
                    swc = driver.findElement(By.id("wallet_code"));
                    submit = driver.findElement(By.id("validate_btn"));
                    code = (String) c.getData(DataFlavor.stringFlavor);
                    seriesOfActions = builder.moveToElement(swc).click().sendKeys(swc, code).click(submit);
                    System.out.println("Redeeming code index number : " + startid);
                    seriesOfActions.perform();
                    suc = checkStatus(driverWait);
                    if(suc) {
                        System.out.println("Successful Redeemed Code with index number : " + startid + " at " + dateFormat.format(Calendar.getInstance().getTime()));
                        String success = wallets.get(startid).getId();
                        successWallet.add(success);
                    } else {
                        System.out.println("Failed Redeemed Code with index number : " + startid + " at " + dateFormat.format(Calendar.getInstance().getTime()));
                        String fail = wallets.get(startid).getId();
                        failedWallet.add(fail);
                    }
                    seriesOfActions = builder.moveToElement(driver.findElement(By.className("modal_close"))).click();
                    seriesOfActions.perform();
                    driver.get("https://store.steampowered.com/account/redeemwalletcode");
                    startid++;
                    if(startid > endid) {
                        System.out.println("Finished Redeemed " + successWallet.size() + " steam wallet code(s). (" + dateFormat.format(Calendar.getInstance().getTime()) + ")");
                        System.out.println("Failed steam wallet : " + printFailedWallet());
                    }
                }
            } else {
                String t = s.nextLine();
                if(t.equals("login")) loggedIn = true;
            }
        }
    }
}
