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

class Main {
    //Program Config
    private static final String WALLETVALUE = "Rp 6 000"; //Value of the steam wallet codes
    static final int TIMERMINUTES = 60; //How long is the timer in minutes
    private static final int MAXREDEEM = 25; //Maximal Redeem amount in timer
    static final boolean DEBUG = false; //Set debug mode, so you can run the bot in debug mode.

    //Define variables to be used in the bot
    private static Console console = System.console();
    private static ArrayList<Wallet> wallets = new ArrayList<>();
    private static ArrayList<String> failedWallet = new ArrayList<>();
    private static ArrayList<String> successWallet = new ArrayList<>();
    private static TimeController timeController = new TimeController();
    private static int redeemAmount = 0;

    //Initialize components
    private static WebDriver driver;
    private static WebDriverWait driverWait;
    private static Actions builder, seriesOfActions;
    private static Scanner s = new Scanner(System.in);
    private static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private static Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();

    private static String printFailedWallet() {
        StringJoiner joiner = new StringJoiner(", ");
        for(String s: failedWallet) {
            joiner.add(s);
        }
        return joiner.toString();
    }

    private static int findWalletIndex(String walletId, ArrayList<Wallet> wallets) {
        for(int i = 0; i < wallets.size(); i++ ) {
            if(walletId.equals(wallets.get(i).getId())) {
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
        c.setContents(stringSelection, null);
        System.out.println("CODE NUMBER " + (wallets.get(id).getId()));
    }

    private static ArrayList<Wallet> getWallet() throws IOException {
        System.out.println("Please enter your steam wallet code (.txt) location: ");
        Scanner location = new Scanner(System.in);
        String code = loadFromFile(location.nextLine());

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
        if (console == null && !DEBUG) {
            System.out.println("Couldn't get Console instance");
            System.exit(0);
        }
        System.setProperty("webdriver.chrome.driver", "D:\\Java\\selenium\\chromedriver.exe");
        driver = new ChromeDriver();
        builder = new Actions(driver);
        driverWait  = new WebDriverWait(driver,30);
        startRedeem();
    }

    private static boolean checkLogin(WebDriver driver) {
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
            return driverWait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("redeem_wallet_success_description"), "You have added " + WALLETVALUE +  " to your Steam Wallet."));
        } catch(TimeoutException ex){
            return false;
        }
    }

    private static void checkError(int startId, int endId) {
        if(startId < 0 || endId < 0) {
            System.out.println("Wrong steam wallet id. Please check again.");
            System.out.println("Exiting bot in 10 seconds");
            wait(10);
            System.exit(0);
        }
    }

    private static void activeCode(int startId, int endId) throws Exception{
        int amount = 0;
        while(startId <= endId) {
            setWallet(startId, wallets);
            WebElement swc = driver.findElement(By.id("wallet_code"));
            WebElement submit = driver.findElement(By.id("validate_btn"));
            String code = (String) c.getData(DataFlavor.stringFlavor);
            seriesOfActions = builder.moveToElement(swc).click().sendKeys(swc, code).click(submit);
            System.out.println("Redeeming code index number : " + startId);
            seriesOfActions.perform();
            boolean suc = checkStatus(driverWait);
            if (suc) {
                System.out.println("Successful Redeemed Code with index number : " + startId + " at " + dateFormat.format(Calendar.getInstance().getTime()));
                String success = wallets.get(startId).getId();
                successWallet.add(success);
            } else {
                System.out.println("Failed Redeemed Code with index number : " + startId + " at " + dateFormat.format(Calendar.getInstance().getTime()));
                String fail = wallets.get(startId).getId();
                failedWallet.add(fail);
            }
            /*seriesOfActions = builder.moveToElement(driver.findElement(By.className("modal_close"))).click();
            seriesOfActions.perform();*/
            driver.get("https://store.steampowered.com/account/redeemwalletcode");
            amount++;
            startId++;
            if (startId > endId) {
                //Calendar lastRedeem = Calendar.getInstance();
                System.out.println("Finished redeemed " + successWallet.size() + " steam wallet code(s). (" + dateFormat.format(Calendar.getInstance().getTime()) + ")");
                System.out.println("Failed steam wallet : " + printFailedWallet());
                redeemAmount = redeemAmount - amount;
                System.out.println(redeemAmount + " Steam Wallet Codes left.");
                if (redeemAmount > 0) {
                    //timeController.sendLastRedeem(lastRedeem);
                    timeController.sendStartId(startId);
                    timeController.startTimer();
                    amount = 0;
                    /*System.out.println("Type 'quit' to exit the bot.");
                    if (s.nextLine().equals("quit")) {
                        System.out.println("Exiting bot in 10 seconds");
                        wait(10);
                        System.exit(0);
                    }*/
                } else if (redeemAmount == 0){
                    System.out.println("Finished redeemed all requested steam wallet codes!");
                    s.nextLine();
                } else {
                    System.out.println("An error has occurred.");
                }
            }
        }
    }

    static void startNextRedeem(int startId, int endId) throws Exception{
        driver.get("https://store.steampowered.com/account/redeemwalletcode");
        redeemAmount = (endId-startId)+1;
        checkError(startId, endId);

        //If the steam wallets that is going to be redeemed more than 25, it takes more than 1 hour, so
        //TimeController will take the action and save the time, so it can start auto redeeming again, when it
        //has already been 1 hour.
        if(redeemAmount > MAXREDEEM) {
            //don't passing the real endId to the TimeController because endId has already been stored before.
            endId = startId + (MAXREDEEM-1); //Set the endId so it now only redeeming 25 codes.
        }
        activeCode(startId, endId);
    }

    private static void startRedeem() throws Exception{
        driver.get("https://store.steampowered.com/login?redir=account%2Fredeemwalletcode");

        System.out.println("===============================================================================");
        WebElement username= driver.findElement(By.id("input_username"));
        System.out.println("Please enter your steam username :");

        seriesOfActions = builder.moveToElement(username).click().sendKeys(username, s.nextLine());
        seriesOfActions.perform();
        WebElement pass = driver.findElement(By.id("input_password"));
        Actions seriesOfAction;
        if(!DEBUG) {
            String password = new String(console.readPassword("Please enter your steam password :\n"));
            seriesOfAction = builder.moveToElement(pass).click().sendKeys(pass, password).sendKeys(Keys.ENTER);
        } else {
            System.out.println("Please enter your steam username :");
            seriesOfAction = builder.moveToElement(pass).click().sendKeys(pass, s.nextLine()).sendKeys(Keys.ENTER);
        }
        seriesOfAction.perform();

        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.id("twofactorcode_entry")));
        System.out.println("Please enter your Steam Guard code: ");
        WebElement guard = driver.findElement(By.id("twofactorcode_entry"));
        seriesOfActions = builder.moveToElement(guard).click().sendKeys(guard, s.nextLine()).sendKeys(Keys.ENTER);
        seriesOfActions.perform();

        System.out.println("Waiting for you to be logged in. Please be patience..");
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.id("wallet_code")));

        driver.get("https://store.steampowered.com/account/redeemwalletcode");
        boolean loggedIn = checkLogin(driver);

        while(true) {
            if(loggedIn) {
                wallets = getWallet();
                System.out.println("Steam Wallet redeem from id :");
                int startId = findWalletIndex(s.nextLine(), wallets);
                System.out.println("Steam Wallet redeem end to id : (Max.25/hour)");
                int endId = findWalletIndex(s.nextLine(), wallets);

                redeemAmount = (endId-startId)+1;
                checkError(startId, endId);

                //If the steam wallets that is going to be redeemed more than 25, it takes more than 1 hour, so
                //TimeController will take the action and save the time, so it can start auto redeeming again, when it
                //has already been 1 hour.
                if(redeemAmount > MAXREDEEM) {
                    timeController.sendEndId(endId); //Pass the real endId to the TimeController
                    endId = startId + (MAXREDEEM-1); //Set the endId so it now only redeeming 25 codes.
                }

                activeCode(startId, endId);
                break;
            } else {
                String t = s.nextLine();
                if(t.equals("login")) loggedIn = true;
            }
        }
    }
}
