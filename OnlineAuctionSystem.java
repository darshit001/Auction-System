import java.io.*;
import java.util.*;

class Player {
    private String name;
    private double bidAmount;
    private String role;

    public Player(String name, double bidAmount, String role) {
        this.name = name;
        this.bidAmount = bidAmount;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(double bidAmount) {
        this.bidAmount = bidAmount;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "\nName : " + name + "  Role : " + role + "  Bid Amount : Rs." + bidAmount;
    }
}

class Team {
    private String name;
    private double budget;
    private List<Player> players;

    public Team(String name, double budget) {
        this.name = name;
        this.budget = budget;
        this.players = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    @Override
    public String toString() {
        return "\nTeam Name : "+name+"\nTotal Budget : Rs."+budget;
    }
}

public class OnlineAuctionSystem {
    private List<Player> availablePlayers = new ArrayList<>();
    private List<Team> teams = new ArrayList<>();
    private Scanner scanner = new Scanner(System.in);
    private BufferedWriter historyLogWriter;
    private BufferedWriter playerDetailsWriter;

    public OnlineAuctionSystem() {
        try {
            historyLogWriter = new BufferedWriter(new FileWriter("history_log.txt", true));
            playerDetailsWriter = new BufferedWriter(new FileWriter("player_details.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addPlayer() {
        System.out.print("\nEnter player name: ");
        String name = scanner.nextLine();
        System.out.print("Enter bid amount: ");
        double bidAmount;
        try {
            bidAmount = scanner.nextDouble();
            scanner.nextLine(); 
        } catch (InputMismatchException e) {
            System.err.println("\nInvalid bid amount. Please enter a valid number.");
            scanner.nextLine(); 
            return;
        }
        System.out.print("Enter player role: ");
        String role = scanner.nextLine();

        Player player = new Player(name, bidAmount, role);
        availablePlayers.add(player);
        System.out.println("\nPlayer added successfully!!! " + player);
    }

    public void removePlayer() {
        System.out.print("\nEnter player name to remove: ");
        String name = scanner.nextLine();

        Iterator<Player> iterator = availablePlayers.iterator();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (player.getName().equals(name)) {
                iterator.remove();
                System.out.println("\nPlayer removed successfully: " + player);
                return;
            }
        }
        System.out.println("\nPlayer with name " + name + " not found.");
    }

    public void addTeam() {
        System.out.print("\nEnter team name: ");
        String name = scanner.nextLine();
        System.out.print("Enter team budget: ");
        double budget;
        try {
            budget = scanner.nextDouble();
            scanner.nextLine(); 
        } catch (InputMismatchException e) {
            System.err.println("\nInvalid budget amount. Please enter a valid number.");
            scanner.nextLine();
            return;
        }

        Team team = new Team(name, budget);
        teams.add(team);
        System.out.println("\nTeam added successfully!!!" + team);
    }

    public void startAuction() {
        System.out.println("\n***** Start Auction *****");
        boolean exit = false;
        while (!exit) {
            System.out.println("\n1. Buy Player for Team");
            System.out.println("2. Sell Player from Team");
            System.out.println("3. End Auction");
            System.out.print("Enter your choice: ");
            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); 

                switch (choice) {
                    case 1:
                        buyPlayerForTeam();
                        break;
                    case 2:
                        sellPlayerFromTeam();
                        break;
                    case 3:
                        System.out.println("\nAuction ended.");
                        exit = true;
                        break;
                    default:
                        System.out.println("\nInvalid choice. Please enter a number between 1 and 3.");
                }
            } catch (InputMismatchException e) {
                System.err.println("\nInvalid input. Please enter a number.");
                scanner.nextLine(); // Consume newline
            }
        }
    }

    public void buyPlayerForTeam() {
        System.out.print("\nEnter player name to buy: ");
        String playerName = scanner.nextLine();

        Player selectedPlayer = null;
        for (Player player : availablePlayers) {
            if (player.getName().equals(playerName)) {
                selectedPlayer = player;
                break;
            }
        }

        if (selectedPlayer == null) {
            System.out.println("\nPlayer not found.");
            return;
        }

        List<Team> potentialBuyers = new ArrayList<>();
        double highestBid = Double.MIN_VALUE;

        // Bid process
        while (true) {
            System.out.println("\nCurrent highest bid: Rs." + highestBid);
            System.out.println("\nEnter bid amount (or 0 to end bidding): ");
            double bidAmount = scanner.nextDouble();
            scanner.nextLine(); // Consume newline

            if (bidAmount == 0) {
                break;
            }

            if (bidAmount <= highestBid) {
                System.out.println("\nBid amount should be higher than current highest bid.");
                continue;
            }

            System.out.println("Enter team name: ");
            String teamName = scanner.nextLine();

            Team team = findTeam(teamName);
            if (team == null) {
                System.out.println("\nTeam not found.");
                continue;
            }

            if (bidAmount > team.getBudget()) {
                System.out.println("\nBid amount exceeds team's budget.");
                continue;
            }

            highestBid = bidAmount;
            potentialBuyers.clear();
            potentialBuyers.add(team);
        }

        if (potentialBuyers.isEmpty()) {
            System.out.println("\nNo team participated in the bidding.");
            return;
        }

        Team winningTeam = potentialBuyers.get(0);
        for (Team team : potentialBuyers) {
            if (team.getBudget() > winningTeam.getBudget()) {
                winningTeam = team;
            }
        }

        System.out.println("\nPlayer " + selectedPlayer.getName() + " bought by team " + winningTeam.getName() +
                " for Rs." + highestBid);
        winningTeam.getPlayers().add(selectedPlayer);
        winningTeam.setBudget(winningTeam.getBudget() - highestBid);
        selectedPlayer.setBidAmount(highestBid); // Set final bid amount for player
        availablePlayers.remove(selectedPlayer);

        // Log the buying history
        try {
            historyLogWriter.write("\nPlayer " + selectedPlayer.getName() + " bought by team " + winningTeam.getName() +
                    " for Rs." + highestBid);
            historyLogWriter.newLine();
            historyLogWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sellPlayerFromTeam() {
        System.out.print("\nEnter player name to sell: ");
        String playerName = scanner.nextLine();
        System.out.print("Enter team name to sell from: ");
        String teamName = scanner.nextLine();

        Team selectedTeam = findTeam(teamName);

        if (selectedTeam == null) {
            System.out.println("\nTeam not found.");
            return;
        }

        Player playerToRemove = null;
        for (Player player : selectedTeam.getPlayers()) {
            if (player.getName().equals(playerName)) {
                playerToRemove = player;
                break;
            }
        }

        if (playerToRemove != null) {
            selectedTeam.removePlayer(playerToRemove);
            availablePlayers.add(playerToRemove);
            double sellingPrice = playerToRemove.getBidAmount(); // Get the selling price
            selectedTeam.setBudget(selectedTeam.getBudget() + sellingPrice); // Increase team's budget
            System.out.println("\nPlayer " + playerName + " sold from team " + teamName + " for Rs." + sellingPrice);

            // Log the selling history
            try {
                historyLogWriter.write("\nPlayer " + playerName + " sold from team " + teamName + " for Rs." + sellingPrice);
                historyLogWriter.newLine();
                historyLogWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("\nPlayer " + playerName + " not found in team " + teamName);
        }
    }

    public void displayAvailablePlayersAndTeams() {
        System.out.println("\nAvailable Players:");
        for (Player player : availablePlayers) {
            System.out.println(player);
        }

        System.out.println("\nTeams:");
        for (Team team : teams) {
            System.out.println(team);
            System.out.println("Players:");
            if (team.getPlayers().isEmpty()) {
                System.out.println("No players in this team.\n");
            } else {
                for (Player player : team.getPlayers()) {
                    System.out.println(player);
                }
            }
        }
    }

    public void displayStats() {
        int totalPlayers = availablePlayers.size();
        int totalTeams = teams.size();

        if (totalPlayers == 0) {
            System.out.println("No players available.");
        } else {
            Player maxBidPlayer = availablePlayers.get(0);
            Player minBidPlayer = availablePlayers.get(0);

            for (Player player : availablePlayers) {
                if (player.getBidAmount() > maxBidPlayer.getBidAmount()) {
                    maxBidPlayer = player;
                }
                if (player.getBidAmount() < minBidPlayer.getBidAmount()) {
                    minBidPlayer = player;
                }
            }

            System.out.println("\nPlayer with highest bid:");
            System.out.println(maxBidPlayer);
            System.out.println("\nPlayer with lowest bid:");
            System.out.println(minBidPlayer);
        }

        System.out.println("\nTotal Players: " + totalPlayers);
        System.out.println("Total Teams: " + totalTeams);

        double totalBudget = 0;
        for (Team team : teams) {
            totalBudget += team.getBudget();
        }
        System.out.println("\nTotal Budget of all Teams: " + totalBudget);
    }

    private Team findTeam(String teamName) {
        for (Team team : teams) {
            if (team.getName().equals(teamName)) {
                return team;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        OnlineAuctionSystem auctionSystem = new OnlineAuctionSystem();
        Scanner scanner = new Scanner(System.in);

        boolean exit = false;
        while (!exit) {
            System.out.println("\nSelect an option:");
            System.out.println("1. Add Player");
            System.out.println("2. Remove Player");
            System.out.println("3. Add Team");
            System.out.println("4. Start Auction");
            System.out.println("5. Display Available Players and Teams");
            System.out.println("6. Display Stats");
            System.out.println("7. Exit");
            System.out.print("\nEnter your choice: ");
            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); 

                switch (choice) {
                    case 1:
                        auctionSystem.addPlayer();
                        break;
                    case 2:
                        auctionSystem.removePlayer();
                        break;
                    case 3:
                        auctionSystem.addTeam();
                        break;
                    case 4:
                        auctionSystem.startAuction();
                        break;
                    case 5:
                        auctionSystem.displayAvailablePlayersAndTeams();
                        break;
                    case 6:
                        auctionSystem.displayStats();
                        break;
                    case 7:
                        exit = true;
                        System.out.println("\n***Exiting the program.***\n***Thank You***\n");
                        break;
                    default:
                        System.out.println("\nInvalid choice. Please enter a number between 1 and 7.");
                }
            } catch (InputMismatchException e) {
                System.err.println("\nInvalid input. Please enter a number.");
                scanner.nextLine(); 
            }
        }

        scanner.close();

        // Close the writers
        auctionSystem.closeWriters();
    }

    private void closeWriters() {
        try {
            historyLogWriter.close();
            playerDetailsWriter.write("\nPlayer Details:");
            playerDetailsWriter.newLine();
            for (Team team : teams) {
                playerDetailsWriter.write("\nTeam: " + team.getName());
                playerDetailsWriter.newLine();
                if (team.getPlayers().isEmpty()) {
                    playerDetailsWriter.write("No players in this team.\n");
                    playerDetailsWriter.newLine();
                } else {
                    for (Player player : team.getPlayers()) {
                        playerDetailsWriter.write(player.toString());
                        playerDetailsWriter.newLine();
                    }
                }
            }
            playerDetailsWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
