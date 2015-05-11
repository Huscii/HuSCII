
package controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;

import models.Job;
import models.JobController;
import models.ParkManager;
import models.User;
import models.UserController;

public class ParkManagerConsole {

    public static JobController jobController;
    public static UserController userController;
    private static Scanner keyboard;
    public static ParkManager parkManager;

    public ParkManagerConsole(User user, JobController jobController,
                              UserController userController) {
        parkManager =
                        new ParkManager(user.getEmail(), user.getFirstName(),
                                        user.getLastName(), user.getRole());
        keyboard = new Scanner(System.in);
        ParkManagerConsole.jobController = jobController;
        ParkManagerConsole.userController = userController;
    }

    public void displayMenu() {

        System.out.println("Welcome To Park Manager Page");
        System.out.println("------------------------------");
        System.out.println("1. Submit a New Job");
        System.out.println("2. View Upcoming Jobs");
        System.out.println("3. View Volunteers");
        System.out.println("4. Logout");
        System.out.println("5. Exit");
        System.out.print("Please select menu choice 1-5: ");
        System.out.println();

        final int menuSelect = keyboard.nextInt();

        switch (menuSelect) {
            case 1:
                submitJob();
                break;
            case 2:
                viewMyJobs();
                break;
            case 3:
                viewVolunteers();
                break;
            case 4:
                final String[] args = {};
                MainConsole.main(args);
            case 5:
                break;
            default:
                System.out.println("Not in a menu choice");
                System.out.println("Please Try Again!\n");
                displayMenu();
                break;
        }
        displayMenu();
    }

    public static void submitJob() {
        if(jobController.checkMaxJobs()) {
            System.out.println("Can't create job, job limit has been reached.");
            return;
        }

        keyboard = new Scanner(System.in);

        System.out.println("Submit a new park job:");
       
        String parkName;
        boolean check = true;
        do{
            System.out.print("Enter Park Name: ");
            parkName = keyboard.nextLine();
            for(String park:parkManager.retrieveManagedParks("testFile.csv")) {
                //search through list of parks
                if(park.equals(parkName)) {
                    check=false;
                    break;
                }
            }
            if(check) {
                System.out.println("You don't manage that park.");
            }
        } while(check);
        
        System.out.print("Enter a Job name (ie trash pickup): ");
        String jobName = keyboard.nextLine();

        String date;
        check = true;
        do {
            System.out.print("Enter a start date & time (MM/DD/YYYY HH:mm AM/PM): ");
             date = keyboard.nextLine();
            GregorianCalendar greg = new GregorianCalendar();
            try {
                greg.setTime(new SimpleDateFormat("MM/dd/yyyy HH:mm a").parse(date));
                check = Job.valiDate(greg);
                if(check) {
                    System.out.println("Date has already occurred "
                                    + "or past 3 months into the future.");
                } else {
                    check = JobController.checkJobWeek(jobController, greg);
                    if(check) {
                        System.out.println("This week already has enough jobs.");
                    }
                }
            }
            catch (ParseException e) {
                System.out.println("Date not in right format");
                e.printStackTrace();
            }
            
        } while(check);
        
        int duration;
        while(true) {
            System.out.print("Enter job duration (in hours): ");
            duration = keyboard.nextInt();
            if(duration>0 && duration<=48) {
                break;
            }
            System.out.println("job duration not valid");
        }
        

        System.out.print("Enter max number of light-duty volunteers needed: ");
        int lightMax = keyboard.nextInt();

        System.out.print("Enter max number of medium-duty volunteers needed: ");
        int medMax = keyboard.nextInt();

        System.out.print("Enter max number of heavy-duty volunteers needed: ");
        int hvyMax = keyboard.nextInt();
        
        parkManager.addJob(jobController, parkName, jobName, date, duration, lightMax, medMax,
                           hvyMax);
    }

    public static void viewMyJobs() {
        System.out.println("Viewing upcoming jobs:");
        System.out.println();

        for (Job j : parkManager.getMyJobs(jobController)) {
            System.out.println(j.getParkName());
            System.out.println(j.getJobName());
            System.out.println("Start date & time: "
                               + new SimpleDateFormat("MM/dd/yyyy HH:mm a").format(j.getDate()
                                               .getTime()));
            System.out.println("Duration: " + j.getJobDuration() + " hours");
            System.out.println(j.getCurrentLight() + " out of " + j.getMaxLight()
                               + " light-duty volunteers.");
            System.out.println(j.getCurrentMedium() + " out of " + j.getMaxMedium()
                               + " medium-duty volunteers.");
            System.out.println(j.getCurrentHard() + " out of " + j.getMaxHard()
                               + " heavy-duty volunteers.");
        }
        System.out.println();

    }

    public static void viewVolunteers() {

        // Display the list pm's jobs:
        System.out.println("Select a job to view its volunteers:");
        List<Job> tempJobs = parkManager.getMyJobs(jobController);
        int i = 1;
        for (Job j : tempJobs) {
            System.out.print(i++ + ") ");
            System.out.print(j.getJobName() + " at ");
            System.out.print(j.getParkName() + " on ");
            System.out.println(new SimpleDateFormat("MM/dd/yyyy HH:mm a").format(j.getDate()
                            .getTime()));
            System.out.println();
        }

        // Ask user to choose from the list
        keyboard = new Scanner(System.in);
        System.out.print("Enter a number from the list: ");
        int choice = keyboard.nextInt();
        while (choice < 0 || choice > tempJobs.size()) {
            System.out.print("Please make a selection from the list: ");
            choice = keyboard.nextInt();
        }

        // Display volunteers of the selected jobs:
        System.out.println("Volunteers:");
        for (String volunteer : tempJobs.get(choice - 1).getVolunteerEmails()) {
            for (User u : userController.getUserList()) {
                if (u.getEmail().equals(volunteer)) {
                    System.out.print(u.getFirstName() + " ");
                    System.out.print(u.getLastName() + ", ");
                    System.out.println(u.getEmail());
                }
            }
        }

        System.out.println(); // Spacer

    }
}
