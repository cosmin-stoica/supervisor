package GlobalPackage;

import javafx.geometry.Pos;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static GlobalPackage.LogMethods.WriteLogDebug;
import static GlobalPackage.LogMethods.WriteLogError;

public class UserHandler
{

    public static class PostazioneUser
    {
        private String postazione;
        private String user;
        private Timestamp data;

        public PostazioneUser(String postazione, String user)
        {
            this.postazione = postazione;
            this.user = user;
        }

        public PostazioneUser(String postazione, String user, Timestamp data)
        {
            this.postazione = postazione;
            this.user = user;
            this.data = data;
        }

        public void setData(Timestamp data)
        {
            this.data = data;
        }

        public void setPostazione(String postazione)
        {
            this.postazione = postazione;
        }

        public void setUser(String user)
        {
            this.user = user;
        }

        public Timestamp getData()
        {
            return data;
        }

        public String getPostazione()
        {
            return postazione;
        }

        public String getUser()
        {
            return user;
        }

        @Override
        public String toString()
        {
            return "PostazioneUser{" + "postazione='" + postazione + '\'' + ", user='" + user + '\'' + '}';
        }
    }

    public static Connection connection;

    private static void demoDB() throws SQLException
    {
        //insertUser("AF60", "500-LAR");
        //insertUser("AF30", "414-BIL");
        //insertUser("AF50", "387-MUL");
        //insertUser("AF10", "311-TOP");
    }

    public static boolean initDB() throws SQLException
    {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(10);
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS postazioni_user (" +
                    "postazione TEXT PRIMARY KEY, " +
                    "user TEXT, " +
                    "data TIMESTAMP DEFAULT NULL CHECK(data IS NULL OR data != '1970-01-01 01:00:00.0')" +
                    ");");
        } catch (SQLException e) {
            WriteLogError("USER_HANDLER::initDB:: " + e.getMessage());
            return false;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                WriteLogError("USER_HANDLER::initDB:: " + e.getMessage());
                return false;
            }
        }

        return true;
    }

    public static List<PostazioneUser> getAllUsers()
    {
        List<PostazioneUser> ToReturn = new ArrayList<>();
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(10);
            ResultSet rs = statement.executeQuery("select * from postazioni_user");

            while (rs.next()) {
                PostazioneUser newUser = new PostazioneUser(rs.getString("postazione"), rs.getString("user"), rs.getTimestamp("data"));
                ToReturn.add(newUser);
            }
        } catch (SQLException e) {
            WriteLogError("USER_HANDLER::getAllUsers:: " + e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                WriteLogError("USER_HANDLER::getAllUsers:: " + e.getMessage());
            }
        }

        System.out.println(ToReturn);
        return ToReturn;
    }

    public static String getPostazioneByUser(String userToSearch)
    {
        String postazione = "none";
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            String query = "SELECT * FROM postazioni_user WHERE user = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userToSearch);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                postazione = rs.getString("postazione");
            }
        } catch (SQLException e) {
            WriteLogError("USER_HANDLER::getPostazioneByUser:: " + e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                WriteLogError("USER_HANDLER::getPostazioneByUser:: " + e.getMessage());
            }
        }
        return postazione;
    }

    public static String getUserByPostazione(String postazioneToSearch)
    {
        String user = "none";
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            String query = "SELECT * FROM postazioni_user WHERE postazione = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, postazioneToSearch);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                user = rs.getString("user");
            }
        } catch (SQLException e) {
            WriteLogError("USER_HANDLER::getPostazioneByUser:: " + e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                WriteLogError("USER_HANDLER::getPostazioneByUser:: " + e.getMessage());
            }
        }
        return user;
    }

    private static boolean insertUser(String postazioneToInsert, String userToInsert, Timestamp data)
    {
        boolean result = false;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            String query = "INSERT INTO postazioni_user VALUES (?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, postazioneToInsert);
            preparedStatement.setString(2, userToInsert);
            if(data!=null)
            preparedStatement.setString(3, String.valueOf(data));
            else
                preparedStatement.setString(3, null);
            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                WriteLogDebug("Utente inserito correttamente");
                result = true;
            } else {
                WriteLogError("ERRORE! Utente non inserito correttamente");
                result = false;
            }
        } catch (SQLException e) {
            WriteLogError("USER_HANDLER::insertUser:: " + e.getMessage());
            result = false;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                WriteLogError("USER_HANDLER::insertUser:: " + e.getMessage());
                result = false;
            }
        }
        return result;
    }

    public static boolean updatePostazione(String postazioneToUpdate, String userToInsert, Timestamp date)
    {
        boolean result = false;
        try {

            try {
                if (insertUser(postazioneToUpdate, userToInsert, date)) {
                    return true;
                }
            } catch (Exception e) {
            }

            connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            String query = "UPDATE postazioni_user SET user = ?, data = ? WHERE postazione = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userToInsert);
            if(date!=null)
                preparedStatement.setString(2, String.valueOf(date));
            else
                preparedStatement.setString(2, null);
            preparedStatement.setString(3, postazioneToUpdate);
            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                WriteLogDebug("Postazione modificata correttamente");
                result = true;
            } else {
                WriteLogError("ERRORE! Postazione non modificata correttamente");
                result = false;
            }
        } catch (SQLException e) {
            WriteLogError("USER_HANDLER::updatePostazione:: " + e.getMessage());
            result = false;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                WriteLogError("USER_HANDLER::updatePostazione:: " + e.getMessage());
                result = false;
            }
        }
        return result;
    }

    public static boolean deleteRowByPostazione(String postazioneToDelete)
    {
        boolean result = false;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            String query = "DELETE FROM postazioni_user WHERE postazione = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, postazioneToDelete);
            int rowsDeleted = preparedStatement.executeUpdate();
            if (rowsDeleted > 0) {
                WriteLogDebug("Postazione eliminata correttamente");
                result = true;
            } else {
                WriteLogError("ERRORE! Postazione non eliminata correttamente");
                result = false;
            }
        } catch (SQLException e) {
            WriteLogError("USER_HANDLER::deleteRowByPostazione:: " + e.getMessage());
            result = false;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                WriteLogError("USER_HANDLER::deleteRowByPostazione:: " + e.getMessage());
                result = false;
            }
        }
        return result;
    }

    private static boolean deleteRowByUser(String userToDelete)
    {
        boolean result = false;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            String query = "DELETE FROM postazioni_user WHERE user = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userToDelete);
            int rowsDeleted = preparedStatement.executeUpdate();
            if (rowsDeleted > 0) {
                WriteLogDebug("User eliminato correttamente");
                result = true;
            } else {
                WriteLogError("ERRORE! User non eliminato correttamente");
                result = false;
            }
        } catch (SQLException e) {
            WriteLogError("USER_HANDLER::deleteRowByUser:: " + e.getMessage());
            result = false;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                WriteLogError("USER_HANDLER::deleteRowByUser:: " + e.getMessage());
                result = false;
            }
        }
        return result;
    }

    public static boolean deleteAll()
    {
        boolean result = false;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            String query = "DELETE FROM postazioni_user";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            int rowsDeleted = preparedStatement.executeUpdate();
            if (rowsDeleted > 0) {
                WriteLogDebug("Tabella utenti svuotata correttamente");
                result = true;
            } else {
                WriteLogError("ERRORE! Tabella utenti non svuotata correttamente");
                result = false;
            }
        } catch (SQLException e) {
            WriteLogError("USER_HANDLER::deleteAll:: " + e.getMessage());
            result = false;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                WriteLogError("USER_HANDLER::deleteAll:: " + e.getMessage());
                result = false;
            }
        }
        return result;
    }

    public static boolean checkPostazioneBusy(String postazioneToCheck)
    {
        if (Objects.equals(getUserByPostazione(postazioneToCheck), "none")) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean checkUserBusy(String userToCheck)
    {
        List<PostazioneUser> AllUsers = getAllUsers();
        for (int i = 0; i < AllUsers.size(); i++) {
            PostazioneUser toCheck = AllUsers.get(i);
            if (toCheck.user.equals(userToCheck)) return true;
        }
        return false;
    }

    public static void main(String[] args)
    {
        try {
            GeneralConfigs.setAllFromConfigIni();
            initDB();
           // System.out.println(updatePostazione("AF25", "311-ASM"));
            getAllUsers();
            Timestamp a = new Timestamp(10);
            updatePostazione("AF35", "311-ASM", null);
            getAllUsers();
        } catch (Exception e) {

        }
    }
}